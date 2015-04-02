package io.collap.bryg.internal.compiler.ast.expression.bitwise;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

public class BinaryBitwiseAndExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseAndExpression (Context context, BrygParser.BinaryBitwiseAndExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseAndExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IAND;
    }

}
