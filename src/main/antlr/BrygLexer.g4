/**
 *  Thanks to
 *      https://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4
 *  for the base of an indentation-sensitive lexer in ANTLR!
 */

lexer grammar BrygLexer;

@header {
    package io.collap.bryg.parser;
}

tokens { INDENT, DEDENT }

@lexer::members {

    // A queue where the next consumed tokens can be added to.
    private java.util.Queue<Token> tokens = new java.util.LinkedList<> ();

    // Keeps track of the indentation level.
    private java.util.Stack<Integer> indents = new java.util.Stack<Integer> () {
        {
            push (0);
        }
    };

    // The amount of opened braces, brackets and parentheses.
    private int opened = 0;

    private String currentBlockString = null; // TODO: Make this a StringBuilder for improved performance!

  @Override
  public void emit(Token t) {
    if (t instanceof CommonToken) {
        ((CommonToken) t).setLine (getLine ());
    }
    super.emit(t);
  }

  public void queueToken(Token t) {
    if (t instanceof CommonToken) {
        ((CommonToken) t).setLine (getLine ());
    }
    tokens.add(t);
  }

  @Override
  public Token nextToken() {
    if (tokens.isEmpty()) {
      queueToken(super.nextToken());
    }

    // Check if the end-of-file is ahead and there are still some DEDENTS expected.
    // There must be more than one indent. The remaining indent is always the 0 indent.
    if (!tokens.isEmpty() && tokens.element().getType() == EOF && indents.size() > 1) {
      // Remove EOF token from the front of the queue.
      Token eofToken = tokens.remove();

      // First queue an extra line break that serves as the end of the statement.
      queueToken(new CommonToken(NEWLINE, "NEWLINE"));

      // Now queue as many DEDENT tokens as we need to reach the leftmost column.
      dedent(0);

      // Now add the EOF token to the end, after the NEWLINE and DEDENT tokens.
      queueToken(eofToken);
    }

    return tokens.poll();
  }

    /**
     *  Replaces tabs according to the Indentation section in the language refernce.
     */
    static int getIndentationCount(String spaces) {
        int count = 0;

        for (char ch : spaces.toCharArray()) {
            switch (ch) {
            case '\t':
                count += 2 - (count % 2);
                break;
            default:
                count++;
            }
        }

        return count;
    }

    /**
     *  Pops indents from the stack until 'indent' is reached and queues DEDENT tokens in the process.
     */
    private void dedent (int indent) {
        while (!indents.isEmpty () && indents.peek () > indent) {
            queueToken (new CommonToken (DEDENT, "DEDENT"));

            // Queue a NEWLINE after each DEDENT, as some parser rules require a NEWLINE after a DEDENT.
            // This is possible since every end of a block may be followed by a NEWLINE.
            queueToken(new CommonToken(NEWLINE, "NEWLINE"));
            indents.pop ();
        }
    }

    private String[] splitTextAndSpaces (String input) {
        return input.split ("\r?\n|\r");
    }

    private void handlePossibleBlockStringDedent (int indent) {
        /* Check if the current line is a blank line. If it is, ignore the dedent. */
        int next = _input.LA (1);
        if (next != '\n' && next != '\r') {
            if (next == EOF) {
                indent = 0; /* Dedent everything. */
            }

            /* Remove \n and/or \r from block string end. */
            int end = currentBlockString.length ();
            char lastChar = currentBlockString.charAt (end - 1);
            if (lastChar == '\n') {
                end -= 1;
                if (currentBlockString.charAt (end - 1) == '\r') {
                    end -= 1;
                }
            }else if (lastChar == '\r') {
                end -= 1;
            }

            if (end != currentBlockString.length ()) {
                currentBlockString = currentBlockString.substring (0, end);
            }

            /* Close block string. */
            queueToken(new CommonToken (String, currentBlockString));
            queueToken(new CommonToken (NEWLINE, "NEWLINE")); /* As a block string counts as a string, much
                                                            like a string on a single line, a NEWLINE
                                                            token must be queued after it. */
            indents.pop (); /* Pop the indent corresponding to the block string. */
            dedent (indent); /* Dedent the rest if applicable. */
            popMode ();
        }
    }

    private boolean isNextCharNewlineOrEOF () {
        int next = _input.LA (1);
        return next == '\n' || next == '\r' || next == EOF;
    }

}


//
//  Keywords
//

NOT                 : 'not';
AND                 : 'and';
OR                  : 'or';
IN                  : 'in';
IS                  : 'is';
EACH                : 'each';
WHILE               : 'while';
ELSE                : 'else';
IF                  : 'if';
MUT                 : 'mut';
VAL                 : 'val';
NULL                : 'null';
TRUE                : 'true';
FALSE               : 'false';
FRAG                : 'frag';
NULLABLE            : 'nullable';
DEFAULT             : 'default';
IMPLICIT            : 'implicit';

Identifier
    :   Letter
        (Letter | Number)*
    ;

Float
    :   Number+ ('.' Number+)? ('f' | 'F')
    ;

Double
    :   Number+ '.' Number+
    ;

Integer
    :   Number+
    ;

Long
    :   Number+ ('l' | 'L')
    ;

String
    :   '\'' ('\\\'' | ~('\'' | '\n' | '\r'))* '\''
    {
        /* The block is needed so the namespace is not polluted. */
        {
            /* Remove single quotations. */
            String string = getText ();
            setText (string.substring (1, string.length () - 1));
        }
    }
    |   ':' (' ')* (~('\n' | '\r' | ' ')) (~('\n' | '\r'))*
    {
        {
            /* Remove colon and trim string. */
            String string = getText ();
            setText (string.substring (1).trim ());
        }
    }
    ;


BlockString
    :  ':' (NewlineChar SPACES?)+   // Newline and spaces can occur more than once to account for blank lines.
    {
        /* Measure indentation. Only if the indentation is greater in the next line is the block string valid! */
        boolean isValidBlockString = false;
        String[] fragments = splitTextAndSpaces (getText ());
        if (fragments.length >= 2) {
            String spaces = fragments[fragments.length - 1];
            int indent = getIndentationCount (spaces);
            int previous = indents.peek ();
            if (indent > previous) {
                isValidBlockString = true;
                indents.push (indent);
            }else if (indent < previous) {
                /* This has to be taken care of, because the spaces are consumed in the token.
                   Otherwise we would possibly forget a DEDENT token. */
                dedent (indent);
            }
        }

        skip ();
        if (isValidBlockString) {
            currentBlockString = "";
            pushMode (block_string);
        }else {
            /* Otherwise, queue a string token with an empty string.
               This corresponds to the second alternative of String,
               which has specifically been narrowed to allow the
               BlockString token. A NEWLINE token has to be queued as well,
               after one was consumed. */
            queueToken(new CommonToken (String, ""));
            queueToken(new CommonToken (NEWLINE, "NEWLINE"));
        }
    }
    ;


NEWLINE
 : NewlineChar SPACES?
   {
     String spaces = getText().replaceAll("[\r\n]+", ""); // TODO: Replace with faster algorithm!
     int next = _input.LA(1);

     // If we're inside a list or on a blank line, ignore all indents,
     // dedents and line breaks.
     // _input.LA(x) always returns. When x > length-pos, it is EOF.
     if (opened <= 0 && next != '\r' && next != '\n' && !(next == '/' && _input.LA(2) == '/')) {
       queueToken(new CommonToken(NEWLINE, "NEWLINE"));

       int indent = getIndentationCount(spaces);
       int previous = indents.peek();

       if (indent > previous) {
         indents.push(indent);
         queueToken(new CommonToken(INDENT, "INDENT"));
       }
       else {
         dedent (indent);
       }
     }

     // Skip all since we only needed to queue some tokens.
     skip();
   }
 ;

NewlineChar
    :   '\r'? '\n'
    |   '\r'
    ;

DOT                 : '.';
COMMA               : ',';
COLON               : ':';
BACKTICK            : '`';
BACKSLASH           : '\\';

AT                  : '@';
QUEST               : '?';

PLUS                : '+';
MINUS               : '-';
MUL                 : '*';
DIV                 : '/';
REM                 : '%';
BNOT                : '~';
BAND                : '&';
BOR                 : '|';
BXOR                : '^';

RGT                 : '>';
RGE                 : '>=';
RLT                 : '<';
RLE                 : '<=';
REQ                 : '==';
RNE                 : '!=';
REFEQ               : '===';
REFNE               : '!==';

SIG_LSHIFT          : '<<';
SIG_RSHIFT          : '>>';
UNSIG_RSHIFT        : '>>>';

ASSIGN              : '=';
ADD_ASSIGN          : '+=';
SUB_ASSIGN          : '-=';
MUL_ASSIGN          : '*=';
DIV_ASSIGN          : '/=';
REM_ASSIGN          : '%=';
BAND_ASSIGN         : '&=';
BOR_ASSIGN          : '|=';
BXOR_ASSIGN         : '^=';
SIG_LSHIFT_ASSIGN   : '<<=';
SIG_RSHIFT_ASSIGN   : '>>=';
UNSIG_RSHIFT_ASSIGN : '>>>=';

LPAREN              : '(' { opened++; };
RPAREN              : ')' { opened--; };

Skip
    :   (SPACES | COMMENT) -> skip
    ;

UnknownChar
    :   .
    ;

fragment
Letter
    :   (   LetterUpper
        |   LetterLower
        )
    ;

fragment
LetterUpper
    :   [A-Z]
    ;

fragment
LetterLower
    :   [a-z]
    ;

fragment
Number
    :   [0-9]
    ;

fragment SPACES
 : [ \t]+
 ;

fragment COMMENT
 : '//' ~[\r\n]*
 ;


mode block_string;

Text
    :   (~('\n' | '\r'))* NewlineChar SPACES?
    {
        /* The strategy here:
            - When a dedent is detected, a String token is emitted with the string content that was captured
              and the mode is popped from the mode stack. Blank lines do not count towards dedents. No DEDENT
              token is emitted.
            - When the indent stays the same only the text is added.
            - When the indent increases, no INDENT token is pushed, because the extra indent is counted
              towards the string. */

        String fullText = getText ();
        String[] fragments = splitTextAndSpaces (fullText);
        if (fragments.length == 2) { /* Newline and spaces. */
            String text = fragments[0];
            String spaces = fragments[1];

            /* Include \r and \n. */
            currentBlockString += text + fullText.substring (text.length (), fullText.length () - spaces.length ());

            /* Check indent. */
            int indent = getIndentationCount (spaces);
            int previous = indents.peek ();
            if (indent > previous) {
                /* Preserve indent, unless the line is a blank line. */
                if (!isNextCharNewlineOrEOF ()) {
                    /* Append the actual text indentation. */
                    currentBlockString += spaces.substring (0, indent - previous);
                }
            }else if (indent < previous) {
                handlePossibleBlockStringDedent (indent);
            }
        }else if (fragments.length == 1) { /* Only text without spaces. */
            currentBlockString += fragments[0];
            handlePossibleBlockStringDedent (0);
        }else {
            /* When no tokens are found, the next line is either blank or
               a proper dedented line. In the latter case, the block string
               needs to be closed. */
            handlePossibleBlockStringDedent (0);
        }

        skip ();
    }
    |   (~('\n' | '\r'))+   // This case happens when the block string ends with EOF.
    {
        currentBlockString += getText ();
        handlePossibleBlockStringDedent (0);
        skip ();
    }
    ;