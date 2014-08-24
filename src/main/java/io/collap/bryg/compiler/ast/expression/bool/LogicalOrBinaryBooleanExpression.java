package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

public class LogicalOrBinaryBooleanExpression extends BinaryBooleanExpression {

    // TODO: Wrap expressions that are not BooleanExpressions in ExpressionBooleanExpression.

    public LogicalOrBinaryBooleanExpression (Context context, BrygParser.BinaryLogicalOrExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        setLine (ctx.getStart ().getLine ());
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        /* Go to the right hand test only when the left hand test failed (hence or). */
        Label rightTest = new Label ();
        ((BinaryBooleanExpression) left).compile (rightTest, nextTrue, false);

        context.getMethodVisitor ().visitLabel (rightTest);
        ((BinaryBooleanExpression) right).compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
