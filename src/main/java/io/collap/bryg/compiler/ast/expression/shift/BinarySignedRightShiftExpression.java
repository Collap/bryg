package io.collap.bryg.compiler.ast.expression.shift;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Opcodes;

public class BinarySignedRightShiftExpression extends BinaryShiftExpression {

    public BinarySignedRightShiftExpression (StandardVisitor visitor, BrygParser.BinaryShiftExpressionContext ctx) {
        super (visitor, ctx);
    }

    public BinarySignedRightShiftExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.ISHR;
    }

}
