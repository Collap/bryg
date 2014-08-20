parser grammar BrygParser;

// TODO: Allow newline characters in parentheses, arguments, etc.
// TODO: Merge preprocessor and lexer, like here: https://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4
// TODO: Find a way to do: if (...) a else b

options {
    tokenVocab = BrygLexer;
}

@header {
    package io.collap.bryg.parser;
}



start
    :   (inDeclaration | NEWLINE)*
        (statementLine | NEWLINE)*
        EOF // EOF is needed for SLL(*) parsing! Otherwise no exception is thrown, which is needed to induce LL(*) parsing.
    ;

inDeclaration
    :   IN type id
        NEWLINE
    ;


statementLine
    :   statement
    ;

statement
    :   ifExpression
    |   eachExpression
    |   variableDeclaration
        NEWLINE
    |   blockFunctionCall
    |   expression
        NEWLINE
    ;

expression
    :   literal                                             # literalExpression
    |   '(' expression ')'                                  # expressionPrecedenceOrder
    |   expression '.' id                                   # accessExpression
    |   expression '.' functionCall                         # functionCallAccessExpression
    |   functionCall                                        # functionCallExpression
    |   variable                                            # variableExpression
    |   '(' type ')' expression                             # castExpression
    |   expression ('++' | '--')                            # unarySuffixExpression
    |   ('+' | '-' | '++' | '--') expression                # unaryPrefixExpression
    |   ('~' | NOT) expression                              # unaryOperationExpression
    |   expression ('*' | '/' | '%') expression             # binaryMultiplicationExpression
    |   expression ('+' | '-') expression                   # binaryAdditionExpression
    |   expression ('<<' | '>>>' | '>>') expression         # binaryShiftExpression
    |   expression ('<=' | '>=' | '>' | '<') expression     # binaryRelationalExpression
    |   expression 'is' type                                # binaryIsExpression
    |   expression ('==' | '!=') expression                 # binaryEqualityExpression
    |   expression '&' expression                           # binaryBitwiseAndExpression
    |   expression '^' expression                           # binaryBitwiseXorExpression
    |   expression '|' expression                           # binaryBitwiseOrExpression
    |   expression AND expression                           # binaryLogicalAndExpression
    |   expression OR expression                            # binaryLogicalOrExpression
    |   <assoc=right> expression
        ( '='
        | '+='
        | '-='
        | '*='
        | '/='
        | '&='
        | '|='
        | '^='
        | '>>='
        | '>>>='
        | '<<='
        | '%='
        )
        expression                                          # binaryAssignmentExpression
    ;

ifExpression
    :   IF
        (   '(' expression ')' statement    // This has to be statement, otherwise an ambiguity is created
                                            // in conjunction with normal parentheses in expressions!
        |   expression NEWLINE block
        )
        (ELSE statementOrBlock)?
    ;

eachExpression
    :   EACH
        (   '(' eachHead ')' statementOrBlock
        |   eachHead NEWLINE block
        )
    ;

eachHead
    :   type? id IN expression
    ;

block
    :   INDENT
        statementLine*
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
    :   (MUT | VAL)
        type? id
        ('=' expression)?
    ;

functionCall
    :   id argumentList
    ;

blockFunctionCall
    :   id argumentList?
        statementOrBlock
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
    : String    # stringLiteral
    | Integer   # integerLiteral
    | Double    # doubleLiteral
    | Float     # floatLiteral
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
        |   EACH
        |   ELSE
        |   IF
        |   MUT
        |   VAL
        )
        '`'
    ;
