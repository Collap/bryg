package io.collap.bryg.compiler.ast.expression.bool;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static bryg.org.objectweb.asm.Opcodes.IF_ACMPNE;

public class ReferenceEqualityExpression extends BinaryBooleanExpression {

    private int operator;

    public ReferenceEqualityExpression (Context context, BrygParser.BinaryReferenceEqualityExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        setLine (ctx.getStart ().getLine ());

        if (left.getType ().isPrimitive () ||
                right.getType ().isPrimitive ()) {
            throw new BrygJitException ("Primitive values can't be compared by reference equality.", getLine ());
        }

        operator = ctx.op.getType ();
    }

    @Override
    public void compile (Label nextFalse, @Nullable Label nextTrue, boolean lastExpressionInChain) {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        // TODO: Use null constants effectively.

        left.compile ();
        right.compile ();

        if (operator == BrygLexer.REFEQ) {
            mv.visitJumpInsn (IF_ACMPNE, nextFalse);
        }else { /* BrygLexer.REFNE */
            mv.visitJumpInsn (IF_ACMPEQ, nextFalse);
        }

        /* Jump to nextTrue label. */
        super.compile (nextFalse, nextTrue, lastExpressionInChain);
    }

}
