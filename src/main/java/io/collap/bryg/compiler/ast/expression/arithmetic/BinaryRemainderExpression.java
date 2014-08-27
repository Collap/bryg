package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Opcodes;

public class BinaryRemainderExpression extends BinaryArithmeticExpression {

    public BinaryRemainderExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                      BrygParser.ExpressionContext rightCtx) {
        super (context, leftCtx, rightCtx);
    }

    public BinaryRemainderExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IREM;
    }

}
