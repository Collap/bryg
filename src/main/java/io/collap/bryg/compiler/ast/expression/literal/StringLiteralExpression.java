package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (StandardVisitor visitor, BrygParser.StringLiteralContext ctx) {
        super (visitor);
        setType (new Type (String.class));
        setLine (ctx.getStart ().getLine ());

        /* Trim quotes, "parentheses" or whatever. */
        value = ctx.String ().getText ();
        int start = 1;
        int end = value.length ();
        if (value.charAt (0) == '\'') {
            end = value.length () - 1;
        }else if (value.charAt (0) == ':') {
            boolean blockString = false;
            int length = value.length ();
            for (int i = 1; i < length; ++i) {
                char c = value.charAt (i);
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                    start = i;
                    if (c == '\u29FC') {
                        start += 1;
                        blockString = true;
                    }
                    break;
                }
            }

            end = length;
            if (blockString) {
                end -= 1;
            }
        }

        value = value.substring (start, end);
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> String
    }

    @Override
    public Object getConstantValue () {
        return value;
    }

}
