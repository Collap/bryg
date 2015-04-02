package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

public class LogicalOrBinaryBooleanExpression extends BinaryBooleanExpression {

    public LogicalOrBinaryBooleanExpression (Context context, BrygParser.BinaryLogicalOrExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        setLine (ctx.getStart ().getLine ());
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        /* Go to the right hand test only when the left hand test failed (hence or). */
        Label rightTest = new Label ();
        ((BooleanExpression) left).compile (rightTest, nextTrue, false);

        context.getMethodVisitor ().visitLabel (rightTest);
        ((BooleanExpression) right).compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
