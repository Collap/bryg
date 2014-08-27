package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Opcodes;

public class BinaryBitwiseXorExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseXorExpression (Context context, BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseXorExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IXOR;
    }

}
