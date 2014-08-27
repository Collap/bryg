package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.IXOR;

// TODO: Promote byte and short?

public class BitwiseNotExpression extends Expression {

    private Expression child;

    public BitwiseNotExpression (Context context, BrygParser.ExpressionContext childCtx) {
        super (context);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) context.getParseTreeVisitor ().visit (childCtx);
        if (!child.getType ().isIntegralType ()) {
            throw new BrygJitException ("The bitwise not expression (~) can only be used on integral types!", getLine ());
        }
        setType (child.getType ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        /*
            ~i is the same as i ^ -1:

                b
              ^ 1

              b = 1 -> b = 0
              b = 0 -> b = 1
        */

        child.compile ();
        // -> T

        if (type.equals (Long.TYPE)) {
            mv.visitLdcInsn (-1L);
        }else { /* byte, short, int */
            mv.visitLdcInsn (-1);
        }
        // -> T

        int xorOp = type.getAsmType ().getOpcode (IXOR);
        mv.visitInsn (xorOp);
        // T, T -> T
    }

}
