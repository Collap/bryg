package io.collap.bryg.internal.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.IFNE;

public class LogicalNotBooleanExpression extends BooleanExpression {

    private Expression child;

    public LogicalNotBooleanExpression (CompilationContext compilationContext, BrygParser.ExpressionContext childCtx) {
        super (compilationContext);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) compilationContext.getParseTreeVisitor ().visit (childCtx);

        if (!child.getType ().similarTo (Boolean.TYPE)) {
            throw new BrygJitException ("The NOT (`not`) operation can only be applied to boolean types.",
                    getLine ());
        }
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        child.compile ();
        // -> boolean

        /* Jump to the false label when the boolean is not 0;
           In other words: Jump to the false label when the boolean is true.*/
        mv.visitJumpInsn (IFNE, nextFalse);

        /* Otherwise, jump to nextTrue if the boolean is false. */
        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
