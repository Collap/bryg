package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends Expression {

    private int value;

    public IntegerLiteralExpression (RenderVisitor visitor, BrygParser.IntegerLiteralContext ctx) {
        super (visitor);
        setType (PrimitiveType._int);
        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (new Integer (value));
        // -> int
    }

}
