package io.collap.bryg.compiler.ast.expression.shift;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Opcodes;

public class BinaryUnsignedRightShiftExpression extends BinaryShiftExpression {

    public BinaryUnsignedRightShiftExpression (Context context, BrygParser.BinaryShiftExpressionContext ctx) {
        super (context, ctx);
    }

    public BinaryUnsignedRightShiftExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IUSHR;
    }

}
