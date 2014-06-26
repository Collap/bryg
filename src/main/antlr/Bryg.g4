grammar Bryg;

@header {
  package io.collap.bryg.parser;
}

start: inDeclaration* statementLine* ;

inDeclaration: 'in' Id ':' type '\n' ;
statementLine: statement '\n' ;
statement: expression ;

expression:
    literal                                           # literalExpression
  | variable                                          # variableExpression
  | '(' expression ')'                                # expressionPrecedenceOrder
  | expression '.' Id                                 # accessExpression
  | 'if' '(' expression ')' statementOrBlock
    ('\n'? 'else' statementOrBlock)?                  # ifExpression
  | 'each' '(' Id 'in' expression ')'
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

variable: Id | variableDeclaration ; // Note that it is possible for a recognized variable to be a trivial function.
variableDeclaration: ('mut' | 'val') Id (':' Id)? ;

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