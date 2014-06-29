grammar Bryg;

// TODO: Allow newline characters in parentheses, arguments, etc.
// Example: eachExpression

@header {
  package io.collap.bryg.parser;
}

start: inDeclaration* statementLine* ;

inDeclaration: 'in' Id ':' type '\n' ;
statementLine: statement '\n' ;
statement: expression
  | variableDeclaration
  ;

expression:
    literal                                           # literalExpression
  | variable                                          # variableExpression
  | '(' expression ')'                                # expressionPrecedenceOrder
  | expression '.' Id                                 # accessExpression
  | 'if' '(' expression ')' statementOrBlock
    ('\n'? 'else' statementOrBlock)?                  # ifExpression
  | 'each' '(' '\n'* Id (':' type)? '\n'* 'in' '\n'* expression ')'
    statementOrBlock                                  # eachExpression
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

block: '\n'? '{' '\n'? statementLine* '\n'? '}' '\n'? ;
statementOrBlock: statement | block ;

variable: Id ; // Note that it is possible for a recognized variable to be a trivial function.
variableDeclaration: ('mut' | 'val') Id (':' type)? ('=' expression)? ;

functionCall: Id ( '(' (argument (',' argument)*)?  ')' )? statementOrBlock? ;
argument: (Id ':')? expression ; // TODO: Allow dashes in HTML attributes.

literal:
    String                                            # stringLiteral
  | Integer                                           # integerLiteral
  ;

Integer: Number+;

type: Id ('<' type (',' type)* '>')? ;

Id: Letter (Letter | Number)* ;
String: '"' ('\\"' | ~('"' | '\n' | '\r'))* '"'
      | '`' ('\\`' | ~('`' | '\n' | '\r'))* '`'
      ;
Ws: [ \t\r]+ -> skip ;

fragment Letter		    : (LetterUpper | LetterLower) ;
fragment LetterUpper  : [A-Z] ;
fragment LetterLower  : [a-z] ;
fragment Number		    : [0-9] ;