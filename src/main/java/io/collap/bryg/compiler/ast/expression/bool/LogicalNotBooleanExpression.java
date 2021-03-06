package io.collap.bryg.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.IFNE;

public class LogicalNotBooleanExpression extends BooleanExpression {

    private Expression child;

    public LogicalNotBooleanExpression (Context context, BrygParser.ExpressionContext childCtx) {
        super (context);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) context.getParseTreeVisitor ().visit (childCtx);

        if (!child.getType ().similarTo (Boolean.TYPE)) {
            throw new BrygJitException ("The NOT (`not`) operation can only be applied to boolean types.",
                    getLine ());
        }
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        child.compile ();
        // -> boolean

        /* Jump to the false label when the boolean is not 0;
           In other words: Jump to the false label when the boolean is true.*/
        mv.visitJumpInsn (IFNE, nextFalse);

        /* Otherwise, jump to nextTrue if the boolean is false. */
        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
