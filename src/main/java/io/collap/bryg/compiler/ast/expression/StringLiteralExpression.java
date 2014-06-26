package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (StandardVisitor visitor, BrygParser.StringLiteralContext ctx) {
        super (visitor);
        setType (new Type (String.class));

        /* Trim quotes. */
        value = ctx.String ().getText ();
        value = value.substring (1, value.length () - 1);
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
