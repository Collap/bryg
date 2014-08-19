package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class FloatLiteralExpression extends Expression {

    private float value;

    public FloatLiteralExpression (StandardVisitor visitor, BrygParser.FloatLiteralContext ctx) {
        super (visitor);
        setType (new Type (Float.TYPE));
        setLine (ctx.getStart ().getLine ());

        value = Float.parseFloat (ctx.Float ().getText ());
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> float
    }

    @Override
    public Object getConstantValue () {
        return value;
    }

}
