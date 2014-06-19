package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.Expression;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

public abstract class BooleanExpression extends Expression {

    protected BooleanExpression (StandardVisitor visitor) {
        super (visitor);
        setType (Boolean.TYPE);
    }

    @Override
    public void compile () {
        throw new UnsupportedOperationException ("BooleanExpressions must be compiled with the special compile function!");
    }

    /**
     * Compiles any boolean expression to a JVM-style if_*cmp chain.
     * @param nextFalse The label to jump to when the expression is false.
     * @param nextTrue The label to jump to when the expression is true.
     * @param lastExpressionInChain Whether the current expression is the last one in the boolean expression overall.
     *                              This variable intends to eliminate unnecessary jumps to a label that actually labels
     *                              the immediately next instruction.
     */
    public abstract void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain);

}
