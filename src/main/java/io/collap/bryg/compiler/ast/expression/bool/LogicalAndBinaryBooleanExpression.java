package io.collap.bryg.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class LogicalAndBinaryBooleanExpression extends BinaryBooleanExpression {

    public LogicalAndBinaryBooleanExpression (Context context, BrygParser.BinaryLogicalAndExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        setLine (ctx.getStart ().getLine ());
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        ((BooleanExpression) left).compile (nextFalse, null, false);
        ((BooleanExpression) right).compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
