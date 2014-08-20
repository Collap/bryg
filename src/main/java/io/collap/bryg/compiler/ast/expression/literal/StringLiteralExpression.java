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
        int start = 0;
        int end = value.length ();
        boolean shouldTrim = false;
        if (value.charAt (0) == '\'') {
            start = 1;
            end = value.length () - 1;
        }else if (value.charAt (0) == ':') {
            start = 1;
            shouldTrim = true;
        }

        if (start > 0 || end < value.length ()) {
            value = value.substring (start, end);
        }

        if (shouldTrim) {
            value.trim ();
        }
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
