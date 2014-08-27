package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import bryg.org.objectweb.asm.Label;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.IFEQ;

/**
 * This boolean expression evaluates an arbitrary expression, which must return a boolean, but is not required to be a
 * subtype of BooleanExpression.
 */
public class ExpressionBooleanExpression extends BooleanExpression {

    private Expression expression;

    public ExpressionBooleanExpression (Context context, Expression expression) {
        super (context);
        this.expression = expression;

        if (!expression.getType ().equals (Boolean.TYPE)) {
            throw new BrygJitException ("Expected a condition, but got an expression that does not return a boolean type",
                getLine ());
        }
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        expression.compile ();
        // -> boolean

        mv.visitJumpInsn (IFEQ, nextFalse); /* Jump to false label when the boolean value is 0. */

        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
