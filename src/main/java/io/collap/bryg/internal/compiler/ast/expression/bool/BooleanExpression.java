package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.GOTO;

// TODO: Put the compile(Label, Label, boolean) method into an interface, so that expressions that
// do not extend this class can still be treated as boolean expressions, without using a WrapperBooleanExpression.

public abstract class BooleanExpression extends Expression {

    protected BooleanExpression(CompilationContext compilationContext, int line) {
        super(compilationContext, line);
        setType(Types.fromClass(Boolean.TYPE));
    }

    /**
     * Compiles the boolean expression in such a way that a boolean result is placed on the stack.
     */
    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        Label nextTrue = new Label();
        Label nextFalse = new Label();
        Label skipFalse = new Label();

        compile(nextFalse, nextTrue, true);

        mv.visitLabel(nextTrue);
        mv.visitLdcInsn(1); /* true */
        // -> I
        mv.visitJumpInsn(GOTO, skipFalse);

        mv.visitLabel(nextFalse);
        mv.visitLdcInsn(0); /* false */
        // -> I

        mv.visitLabel(skipFalse);
    }

    /**
     * TODO: Remove this implementation and create a separate method for it to reduce confusion.
     * Compiles any boolean expression to a JVM-style if_*cmp chain.
     * This particular implementation can be used to jump to the nextTrue label if it exists. This means that
     * not overriding this method will result in the boolean expression to be always true.
     *
     * @param nextFalse             The label to jump to when the expression is false.
     * @param nextTrue              The label to jump to when the expression is true.
     * @param lastExpressionInChain Whether the current expression is the last one in the boolean expression overall.
     *                              This variable intends to eliminate unnecessary jumps to a label that actually labels
     *                              the immediately next instruction.
     */
    public void compile(Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        /* At this point the expression is supposed to be true. */
        if (nextTrue != null && !lastExpressionInChain) {
            compilationContext.getMethodVisitor().visitJumpInsn(GOTO, nextTrue);
        }
    }

}
