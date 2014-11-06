parser grammar BrygParser;

// TODO: Find a way to do: if (...) a else b

// TODO: Currently, a statement li -b is interpreted as a subtraction. (Before 1.0)
//  Parentheses give the hint to the compiler that li is a normal function call with -b as a parameter:
//      li (-b)
//  The only feasible way to fix this is
//      li
//        -b
//  which is not intuitive.
//  Maybe we can supply the parser with additional information about declared variables and functions,
//  to allow the compiler to decide whether it is a variable or a function?
//  Alternative: Change the binary subtraction node to a function call with negation as a statement,
//  if the compiler finds that binary subtraction is not possible, because either expression's type is 'void'.

options {
    tokenVocab = BrygLexer;
}

@header {
    package io.collap.bryg.parser;
}


start
    :   (inDeclaration | NEWLINE)*
        (statement | NEWLINE)*
        (fragmentFunction | NEWLINE)*
        EOF // EOF is needed for SLL(*) parsing! Otherwise no exception is thrown, which is needed to induce LL(*) parsing.
    ;

inDeclaration
    :   qualifier=(IN | OPT) type id
        NEWLINE
    ;


statement
    :   ifStatement
    |   eachStatement
    |   whileStatement
    |   variableDeclaration
        NEWLINE
    |   blockFunctionCall
    |   expression
        NEWLINE
    |   statementFunctionCall   /* This rule has to be placed after expression, because otherwise an expression
                                   like a - 7 would be interpreted as a statementFunctionCall with an arithmetic
                                   negation (that leads to the number -7) instead of a binary subtraction. */
    |   templateFragmentCall
    ;

/**
 *  This rule is executed separately when the compiler finds an unescaped interpolation sequence (\{...}) in a string.
 */
interpolation
    :   expression NEWLINE
    ;


fragmentFunction
    :   FRAG id NEWLINE
        fragmentBlock
    ;

fragmentBlock
    :   INDENT
        (inDeclaration | NEWLINE)*
        (statement | NEWLINE)*
        DEDENT
    ;


closure
    :   INDENT
        (inDeclaration | NEWLINE)*
        (statement | NEWLINE)*
        DEDENT
    ;

expression
    :   literal                                             # literalExpression
    |   '(' expression ')'                                  # expressionPrecedenceOrder
    |   expression '.' id                                   # accessExpression
    |   expression '.' functionCall                         # methodCallExpression
    |   functionCall                                        # functionCallExpression
    |   variable                                            # variableExpression
    |   '(' type ')' expression                             # castExpression
    |   expression op=('++' | '--')                         # unaryPostfixExpression
    |   op=('-' | '++' | '--') expression                   # unaryPrefixExpression
    |   op=('~' | NOT) expression                           # unaryOperationExpression
    |   expression op=('*' | '/' | '%') expression          # binaryMulDivRemExpression
    |   expression op=('+' | '-') expression                # binaryAddSubExpression
    |   expression op=('<<' | '>>>' | '>>') expression      # binaryShiftExpression
    |   expression op=('<=' | '>=' | '>' | '<') expression  # binaryRelationalExpression
    |   expression 'is' type                                # binaryIsExpression                // TODO: Implement (Before 1.0); Possibly change 'is' back to 'instanceof'.
    |   expression op=('==' | '!=') expression              # binaryEqualityExpression
    |   expression op=('===' | '!==') expression            # binaryReferenceEqualityExpression
    |   expression '&' expression                           # binaryBitwiseAndExpression
    |   expression '^' expression                           # binaryBitwiseXorExpression
    |   expression '|' expression                           # binaryBitwiseOrExpression
    |   expression AND expression                           # binaryLogicalAndExpression
    |   expression OR expression                            # binaryLogicalOrExpression
    |   <assoc=right> expression
        op=
        ( '='
        | '+='
        | '-='
        | '*='
        | '/='
        | '%='
        | '&='
        | '|='
        | '^='
        | '>>='
        | '>>>='
        | '<<='
        )
        expression                                          # binaryAssignmentExpression
    ;

ifStatement
    :   IF
        (   '(' expression ')' statement    // This has to be statement, otherwise an ambiguity is created
                                            // in conjunction with normal parentheses in expressions!
        |   expression NEWLINE block
        )
        (ELSE statementOrBlock)?
    ;

eachStatement
    :   EACH
        (   '(' eachHead ')' statementOrBlock
        |   eachHead NEWLINE block
        )
    ;

eachHead
    :   type? element=id (',' index=id)? IN expression
    ;

whileStatement
    :   WHILE
        (   '(' condition=expression ')' statement
        |   condition=expression NEWLINE block
        )
    ;

block
    :   INDENT
        statement*
        DEDENT
    ;

statementOrBlock
    :   NEWLINE block
    |   statement
    ;

variable
    :   id  // Note that it is possible for a recognized variable to be a trivial function.
    ;

variableDeclaration
    :   mutability=(MUT | VAL)
        type? id
        ('=' expression)?
    ;

functionCall
    :   id argumentList
    ;

blockFunctionCall
    :   id argumentList?
        NEWLINE block
    ;

statementFunctionCall
    :   id argumentList?
        statement
    ;

/* Calls functions of other templates. By default, 'render' is called. */
templateFragmentCall
    :   templateId frag=id? argumentList?
        NEWLINE closure?
    ;

templateId
    :   '@' currentPackage='.'? (id '.')* id
    ;

argumentList
    :   '(' (argument (',' argument)*)? ')'
    ;

argument
    :   argumentId? expression argumentPredicate?
    ;

argumentId
    :   id ('-' id)*
    ;

argumentPredicate
    :   IF expression
    ;

literal
    :   String                  # stringLiteral
    |   Integer                 # integerLiteral
    |   Long                    # longLiteral
    |   Double                  # doubleLiteral
    |   Float                   # floatLiteral
    |   NULL                    # nullLiteral
    |   value=(TRUE | FALSE)    # booleanLiteral
    ;

type
    :   id
        ('<' type (',' type)* '>')?
    ;

id
    :   Identifier
    |   '`'
        (   Identifier
        |   NOT
        |   AND
        |   OR
        |   IN
        |   OPT
        |   IS
        |   EACH
        |   WHILE
        |   ELSE
        |   IF
        |   MUT
        |   VAL
        |   NULL
        |   TRUE
        |   FALSE
        )
        '`'
    ;
