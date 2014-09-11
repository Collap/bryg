package io.collap.bryg.compiler.ast.expression.shift;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;

public class BinarySignedRightShiftExpression extends BinaryShiftExpression {

    public BinarySignedRightShiftExpression (Context context, BrygParser.BinaryShiftExpressionContext ctx) {
        super (context, ctx);
    }

    public BinarySignedRightShiftExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.ISHR;
    }

}
