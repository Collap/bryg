package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.ast.expression.coercion.UnboxingExpression;
import io.collap.bryg.compiler.ast.expression.unary.CastExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.compiler.util.BoxingUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import static bryg.org.objectweb.asm.Opcodes.IXOR;

public class BitwiseNotExpression extends Expression {

    private Expression child;

    public BitwiseNotExpression (Context context, BrygParser.ExpressionContext childCtx) {
        super (context);
        setLine (childCtx.getStart ().getLine ());
        child = (Expression) context.getParseTreeVisitor ().visit (childCtx);

        if (!child.getType ().isPrimitive ()) {
            /* Possibly unbox. */
            Type primitiveType = child.getType ().getPrimitiveType ();
            if (primitiveType == null) {
                throw new BrygJitException ("The expression with the type " + child.getType () + " can not be unboxed!", getLine ());
            }
            child = new UnboxingExpression (context, child, primitiveType);
        }

        if (!child.getType ().isIntegralType ()) {
            throw new BrygJitException ("The bitwise not expression (~) can only be used on integral types (Current type: " + child.getType () + ")!", getLine ());
        }

        /* Promote byte and short to int, since there are no opcodes for BNOT for byte and short. */
        if (child.getType ().similarTo (Byte.TYPE) || child.getType ().similarTo (Short.TYPE)) {
            child = new CastExpression (context, Types.fromClass (Integer.TYPE), child, getLine ());
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

        if (type.similarTo (Long.TYPE)) {
            mv.visitLdcInsn (-1L);
        }else { /* byte, short, int */
            mv.visitLdcInsn (-1);
        }
        // -> T

        int xorOp = type.getOpcode (IXOR);
        mv.visitInsn (xorOp);
        // T, T -> T
    }

}
