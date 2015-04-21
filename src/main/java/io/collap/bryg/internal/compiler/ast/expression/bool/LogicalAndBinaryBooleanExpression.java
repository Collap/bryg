package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class LogicalAndBinaryBooleanExpression extends BinaryBooleanExpression {

    public LogicalAndBinaryBooleanExpression(CompilationContext compilationContext, BrygParser.BinaryLogicalAndExpressionContext ctx) {
        super(compilationContext, ctx.expression(0), ctx.expression(1));
    }

    @Override
    public void compile(Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        ((BooleanExpression) left).compile(nextFalse, null, false);
        ((BooleanExpression) right).compile(nextFalse, nextTrue, lastExpressionInChain);
    }

}
