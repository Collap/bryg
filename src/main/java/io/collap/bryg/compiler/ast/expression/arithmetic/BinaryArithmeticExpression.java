package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

// TODO: Evaluate constant expressions more efficiently.

public abstract class BinaryArithmeticExpression extends BinaryExpression {

    protected BinaryArithmeticExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                          BrygParser.ExpressionContext rightCtx) {
        super (context, leftCtx, rightCtx);
        setupType ();
    }

    protected BinaryArithmeticExpression (Context context, int line) {
        super (context, line);
        setupType ();
    }

    protected BinaryArithmeticExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
        setupType ();
    }

    protected void setupType () {
        setType (CoercionUtil.getTargetType (left.getType (), right.getType (), getLine ()));
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (type.getJavaType ().isPrimitive ()) {
            CoercionUtil.attemptBinaryCoercion (context, left, right, type);
            int op = type.getAsmType ().getOpcode (getOpcode ());
            mv.visitInsn (op);
        }else {
            throw new BrygJitException ("Unexpected type " + type, getLine ());
        }
    }

    /**
     * @return The opcode for the arithmetic expression in the integer (IADD, ISUB, etc.) form.
     */
    protected abstract int getOpcode ();

}
