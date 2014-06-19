package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

public class IntegerLiteralExpression extends Expression {

    private int value;

    public IntegerLiteralExpression (StandardVisitor visitor, BrygParser.IntegerLiteralContext ctx) {
        super (visitor);
        setType (Integer.TYPE);
        value = Integer.parseInt (ctx.Integer ().getText ());
    }

    @Override
    public void compile () {
        visitor.getMethod ().visitLdcInsn (new Integer (value));
        // -> int
    }

}
