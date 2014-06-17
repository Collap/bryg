package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

public class LogicalAndBinaryBooleanExpression extends BinaryBooleanExpression {

    public LogicalAndBinaryBooleanExpression (RenderVisitor visitor, BrygParser.BinaryLogicalAndExpressionContext ctx) {
        super (visitor, ctx.expression (0), ctx.expression (1));
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        ((BinaryBooleanExpression) left).compile (nextFalse, null, false);
        ((BinaryBooleanExpression) right).compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
