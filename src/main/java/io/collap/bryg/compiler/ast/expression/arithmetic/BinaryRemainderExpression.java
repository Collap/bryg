package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Opcodes;

public class BinaryRemainderExpression extends BinaryArithmeticExpression {

    public BinaryRemainderExpression (StandardVisitor visitor, BrygParser.ExpressionContext leftCtx,
                                      BrygParser.ExpressionContext rightCtx) {
        super (visitor, leftCtx, rightCtx);
    }

    public BinaryRemainderExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
    }

    @Override
    protected int getOpcode () {
        return Opcodes.IREM;
    }

}
