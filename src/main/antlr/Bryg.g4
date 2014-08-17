grammar Bryg;

// TODO: Allow newline characters in parentheses, arguments, etc.
// Example: eachExpression

@header {
  package io.collap.bryg.parser;
}

start: inDeclaration* statementLine* ;

inDeclaration: 'in' type Id '\n' ;
statementLine: statement '\n' ;
statement:
    expression
  | variableDeclaration
  ;

expression:
    literal                                           # literalExpression
  | '(' expression ')'                                # expressionPrecedenceOrder
  | expression '.' Id                                 # accessExpression
  | 'if' ('(' expression ')' statementOrBlock | expression block)
    ('\n'? 'else' statementOrBlock)?                  # ifExpression
  | 'each'
      ('(' '\n'* type? Id '\n'* 'in' '\n'* expression ')' statementOrBlock
     | type? Id 'in' expression block)                # eachExpression
  | variable                                          # variableExpression // Note: variable has to be placed before functionCall
  | functionCall                                      # functionCallExpression
  | '(' type ')' expression                           # castExpression
  | expression ('++' | '--')                          # unarySuffixExpression
  | ('+' | '-' | '++' | '--') expression              # unaryPrefixExpression
  | ('~' | 'not') expression                          # unaryOperationExpression
  | expression ('*' | '/' | '%') expression           # binaryMultiplicationExpression
  | expression ('+' | '-') expression                 # binaryAdditionExpression
  | expression ('<<' | '>>>' | '>>') expression       # binaryShiftExpression
  | expression ('<=' | '>=' | '>' | '<') expression   # binaryRelationalExpression
  | expression 'is' type                              # binaryIsExpression
  | expression ('==' | '!=') expression               # binaryEqualityExpression
  | expression '&' expression                         # binaryBitwiseAndExpression
  | expression '^' expression                         # binaryBitwiseXorExpression
  | expression '|' expression                         # binaryBitwiseOrExpression
  | expression 'and' expression                       # binaryLogicalAndExpression
  | expression 'or' expression                        # binaryLogicalOrExpression
  | <assoc=right> expression
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
    expression                                        # binaryAssignmentExpression
  ;

block: '\n'? '\u29FC' '\n'? statementLine* '\n'? '\u29FD' '\n'? ;
statementOrBlock: statement | block ;

variable: Id ; // Note that it is possible for a recognized variable to be a trivial function.
variableDeclaration: ('mut' | 'val') type? Id ('=' expression)? ;

functionCall: Id ( '(' (argument (',' argument)*)?  ')' )? statementOrBlock? ;
argument: Id? expression ; // TODO: Allow dashes in HTML attributes.

literal:
    String                                            # stringLiteral
  | Integer                                           # integerLiteral
  ;

Integer: Number+;

type: Id ('<' type (',' type)* '>')? ;

Id: Letter (Letter | Number)* ;
String: '\'' ('\\\'' | ~('\'' | '\n' | '\r'))* '\''
      | ':' Ws? '\n'? Ws? '\u29FC' (~'\u29FD')* '\u29FD'
      | ':' (~('\n' | '\r'))*
      ;
Ws: [ \t\r]+ -> skip ;

fragment Letter		    : (LetterUpper | LetterLower) ;
fragment LetterUpper  : [A-Z] ;
fragment LetterLower  : [a-z] ;
fragment Number		    : [0-9] ;