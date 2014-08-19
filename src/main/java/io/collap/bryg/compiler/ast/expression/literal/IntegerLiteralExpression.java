package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends Expression {

    private int value;

    public IntegerLiteralExpression (StandardVisitor visitor, BrygParser.IntegerLiteralContext ctx) {
        super (visitor);
        setType (new Type (Integer.TYPE));
        setLine (ctx.getStart ().getLine ());

        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (value);
        // -> int
    }

    @Override
    public Object getConstantValue () {
        return value;
    }

}
