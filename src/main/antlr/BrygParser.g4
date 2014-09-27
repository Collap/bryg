parser grammar BrygParser;

// TODO: Find a way to do: if (...) a else b

options {
    tokenVocab = BrygLexer;
}

@header {
    package io.collap.bryg.parser;
}


start
    :   (inDeclaration | NEWLINE)*
        (statement | NEWLINE)*
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
        NEWLINE
    ;

/**
 *  This rule is executed separately when the compiler finds an unescaped interpolation sequence (\{...}) in a string.
 */
interpolation
    :   expression NEWLINE
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
    |   op=('+' | '-' | '++' | '--') expression             # unaryPrefixExpression
    |   op=('~' | NOT) expression                           # unaryOperationExpression
    |   expression op=('*' | '/' | '%') expression          # binaryMulDivRemExpression
    |   expression op=('+' | '-') expression                # binaryAddSubExpression
    |   expression op=('<<' | '>>>' | '>>') expression      # binaryShiftExpression
    |   expression op=('<=' | '>=' | '>' | '<') expression  # binaryRelationalExpression
    |   expression 'is' type                                # binaryIsExpression                // TODO: Implement
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
    :   templateId argumentList?
    ;

templateId
    :   '@' currentPackage='.'? (id '.')* id
    ;

argumentList
    :   '(' (argument (',' argument)*)? ')'
    ;

argument
    :   argumentId? expression
    ;

argumentId
    :   id ('-' id)*
    ;

literal
    :   String                  # stringLiteral
    |   Integer                 # integerLiteral
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
