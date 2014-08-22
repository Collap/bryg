package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Opcodes;

public class BinaryBitwiseXorExpression extends BinaryBitwiseExpression {

    public BinaryBitwiseXorExpression (StandardVisitor visitor, BrygParser.BinaryBitwiseXorExpressionContext ctx) {
        super (visitor, ctx.expression (0), ctx.expression (1));
    }

    public BinaryBitwiseXorExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IXOR;
    }

}
