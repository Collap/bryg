package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Opcodes;

public class BinaryBitwiseOrExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseOrExpression (Context context, BrygParser.BinaryBitwiseOrExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseOrExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IOR;
    }

}
