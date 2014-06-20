package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (StandardVisitor visitor, BrygParser.StringLiteralContext ctx) {
        super (visitor);
        setType (String.class);

        /* Trim quotes. */
        value = ctx.String ().getText ();
        if (value.charAt (0) == '"') {
            value = value.substring (1);
        }
        int lastCharIndex = value.length () - 1;
        if (value.charAt (lastCharIndex) == '"') {
            value = value.substring (0, lastCharIndex);
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
