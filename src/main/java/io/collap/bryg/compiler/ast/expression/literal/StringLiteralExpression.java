package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

// TODO: Convert '\n', '\'', etc.

public class StringLiteralExpression extends Expression {

    private String value;

    public StringLiteralExpression (StandardVisitor visitor, BrygParser.StringLiteralContext ctx) {
        super (visitor);
        setType (new Type (String.class));
        setLine (ctx.getStart ().getLine ());
        value = ctx.String ().getText ();
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
