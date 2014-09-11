package io.collap.bryg.compiler.ast.expression.arithmetic;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;

public class BinaryDivisionExpression extends BinaryArithmeticExpression {

    public BinaryDivisionExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                     BrygParser.ExpressionContext rightCtx) {
        super (context, leftCtx, rightCtx);
    }

    public BinaryDivisionExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IDIV;
    }

}
