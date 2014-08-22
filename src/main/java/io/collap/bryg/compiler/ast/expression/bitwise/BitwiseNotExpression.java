package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static org.objectweb.asm.Opcodes.*;

// TODO: Promote byte and short?

public class BitwiseNotExpression extends Expression {

    private Expression child;

    public BitwiseNotExpression (StandardVisitor visitor, BrygParser.ExpressionContext childCtx) {
        super (visitor);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) visitor.visit (childCtx);
        if (!child.getType ().isIntegralType ()) {
            throw new BrygJitException ("The bitwise not expression (~) can only be used on integral types!", getLine ());
        }
        setType (child.getType ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = visitor.getMethod ();

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
