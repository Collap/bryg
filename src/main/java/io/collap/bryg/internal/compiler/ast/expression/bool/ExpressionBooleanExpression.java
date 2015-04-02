package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.util.BoxingUtil;
import io.collap.bryg.BrygJitException;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.IFEQ;

/**
 * This boolean expression evaluates an arbitrary expression, which must return a boolean, but is not required to be a
 * subtype of BooleanExpression.
 *
 * Automatically unboxes Boolean boxes.
 *
 * Expressions that return a boolean value are automatically wrapped in this node by the StandardVisitor.
 */
public class ExpressionBooleanExpression extends BooleanExpression {

    private Expression expression;

    public ExpressionBooleanExpression (Context context, Expression expression) {
        super (context);
        setLine (expression.getLine ());
        this.expression = expression;

        if (!expression.getType ().similarTo (Boolean.TYPE)) {
            /* Attempt unboxing. */
            Expression unboxedExpression = null;
            if (expression.getType ().similarTo (Boolean.class)) {
                unboxedExpression = BoxingUtil.createUnboxingExpression (context, expression);
            }
            if (unboxedExpression == null) {
                throw new BrygJitException ("Expected a condition, but got an expression that does not return a boolean type",
                        getLine ());
            }
            this.expression = unboxedExpression;
        }
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        expression.compile ();
        // -> boolean

        /* Jump to false label when the boolean value is 0. */
        mv.visitJumpInsn (IFEQ, nextFalse);

        /* Handle nextTrue. */
        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
