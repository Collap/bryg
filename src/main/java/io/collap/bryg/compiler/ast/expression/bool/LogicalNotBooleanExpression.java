package io.collap.bryg.compiler.ast.expression.bool;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;
import org.objectweb.asm.Label;

import javax.annotation.Nullable;

import static org.objectweb.asm.Opcodes.*;

public class LogicalNotBooleanExpression extends BooleanExpression {

    // TODO: Wrap expressions that are not BooleanExpressions in ExpressionBooleanExpression.

    private Expression child;

    public LogicalNotBooleanExpression (StandardVisitor visitor, BrygParser.ExpressionContext childCtx) {
        super (visitor);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) visitor.visit (childCtx);

        if (!child.getType ().equals (Boolean.TYPE)) {
            throw new BrygJitException ("The NOT (`not`) operation can only be applied to boolean types.",
                    getLine ());
        }
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = visitor.getMethod ();

        child.compile ();
        // -> boolean

        /* Jump to the false label when the boolean is not 0;
           In other words: Jump to the false label when the boolean is true.*/
        mv.visitJumpInsn (IFNE, nextFalse);

        /* Otherwise, jump to nextTrue if the boolean is false. */
        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}