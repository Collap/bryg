parser grammar BrygParser;

// TODO: Find a way to do: if (...) a else b

options {
    tokenVocab = BrygLexer;
}

@header {
    package io.collap.bryg.parser;
}


start
    :   (fieldDeclaration | NEWLINE)*
        (fragmentFunction | NEWLINE)*
        EOF // EOF is needed for SLL(*) parsing! Otherwise no exception is thrown, which is needed to fallback to LL(*) parsing.
    ;


// TODO: Nullable?
fieldDeclaration
    :   type id
        NEWLINE
    ;


statement
    :   ifStatement
    |   eachStatement
    |   whileStatement
    |   variableDeclaration
        NEWLINE
    |   blockFunctionCall
    |   statementFunctionCall  // TODO: Is it alright that this is placed before the expression now?
    |   expression
        NEWLINE
    ;


/**
 *  This rule is executed separately when the compiler finds an unescaped interpolation sequence (\{...}) in a string.
 */
interpolation
    :   expression NEWLINE
    ;


fragmentFunction
    :   (DEFAULT FRAG | DEFAULT FRAG id | FRAG id) parameterList?
        fragmentBlock
    ;

fragmentBlock
    :   INDENT
        (statement | NEWLINE)*
        DEDENT
    ;

parameterList
    :   '(' parameterDeclaration? (',' parameterDeclaration)* ')'
    ;

parameterDeclaration
    :   nullable=NULLABLE? type id
    ;

expression
    :   literal                                             # literalExpression
    |   '(' expression ')'                                  # expressionPrecedenceOrder
    |   expression '.' id                                   # accessExpression
    |   expression '.' functionCall                         # methodCallExpression
    |   functionCall                                        # functionCallExpression
    |   variable                                            # variableExpression
    |   closure                                             # closureExpression
    |   templateInstantiation                               # templateInstantiationExpression
    |   '(' type ')' expression                             # castExpression
    |   '(' '-' expression ')'                              # unaryNegationExpression
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

closure
    :   '\\' parameterList?
        closureBody
    ;

closureBody
    :   INDENT
        (statement | NEWLINE)*
        DEDENT
    ;

templateInstantiation
    :   templateId argumentList
    ;

block
    :   INDENT
        (statement | NEWLINE)*
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

argumentList
    :   '(' argument? (',' argument)* ')'
    ;

argument
    :   argumentId? expression argumentPredicate?
    ;

argumentId
    :   id
    |   '`' (Identifier | keyword) ('-' (Identifier | keyword))* '`'
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
    :   id ('<' type (',' type)* '>')?
    |   templateId
    ;

templateId
    :   '@' currentPackage='.'? (id '.')* id
    ;

id
    :   Identifier
    |   '`' keyword '`'
    ;

keyword
    :   NOT
    |   AND
    |   OR
    |   IN
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
    |   FRAG
    |   NULLABLE
    |   DEFAULT
    ;