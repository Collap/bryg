package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

public class LogicalAndBinaryBooleanExpression extends BinaryBooleanExpression {

    // TODO: Wrap expressions that are not BooleanExpressions in ExpressionBooleanExpression.

    public LogicalAndBinaryBooleanExpression (Context context, BrygParser.BinaryLogicalAndExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        setLine (ctx.getStart ().getLine ());
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        ((BinaryBooleanExpression) left).compile (nextFalse, null, false);
        ((BinaryBooleanExpression) right).compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
