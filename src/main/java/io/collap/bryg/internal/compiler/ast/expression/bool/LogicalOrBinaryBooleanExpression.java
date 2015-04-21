package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class LogicalOrBinaryBooleanExpression extends BinaryBooleanExpression {

    public LogicalOrBinaryBooleanExpression(CompilationContext compilationContext, BrygParser.BinaryLogicalOrExpressionContext ctx) {
        super(compilationContext, ctx.expression(0), ctx.expression(1));
    }

    @Override
    public void compile(Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        /* Go to the right hand test only when the left hand test failed (hence or). */
        Label rightTest = new Label();
        ((BooleanExpression) left).compile(rightTest, nextTrue, false);

        compilationContext.getMethodVisitor().visitLabel(rightTest);
        ((BooleanExpression) right).compile(nextFalse, nextTrue, lastExpressionInChain);
    }

}
