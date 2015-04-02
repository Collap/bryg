package io.collap.bryg.internal.compiler.ast.expression.arithmetic;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

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
