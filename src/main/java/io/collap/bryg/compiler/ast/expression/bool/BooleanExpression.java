package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.type.Type;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

import static org.objectweb.asm.Opcodes.GOTO;

public abstract class BooleanExpression extends Expression {

    protected BooleanExpression (StandardVisitor visitor) {
        super (visitor);
        setType (new Type (Boolean.TYPE));
    }

    /**
     * Compiles the boolean expression in such a way that a boolean result is placed on the stack.
     */
    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        Label nextFalse = new Label ();
        Label skipFalse = new Label ();

        compile (nextFalse, null, true);

        method.visitLdcInsn (1); /* true */
        // -> I
        method.visitJumpInsn (GOTO, skipFalse);

        method.visitLabelInSameFrame (nextFalse);
        method.visitLdcInsn (0); /* false */
        // -> I

        method.visitLabelInSameFrame (skipFalse);
    }

    /**
     * Compiles any boolean expression to a JVM-style if_*cmp chain.
     * This particular implementation can be used to jump to the nextTrue label if it exists. This means that
     * not overriding this method will result in the boolean expression to be always true.
     * @param nextFalse The label to jump to when the expression is false.
     * @param nextTrue The label to jump to when the expression is true.
     * @param lastExpressionInChain Whether the current expression is the last one in the boolean expression overall.
     *                              This variable intends to eliminate unnecessary jumps to a label that actually labels
     *                              the immediately next instruction.
     */
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        /* At this point the expression is supposed to be true. */
        if (nextTrue != null && !lastExpressionInChain) {
            visitor.getMethod ().visitJumpInsn (GOTO, nextTrue);
        }
    }

}
