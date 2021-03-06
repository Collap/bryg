package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.util.CoercionUtil;
import io.collap.bryg.compiler.util.Pair;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryBitwiseExpression extends BinaryExpression {

    protected BinaryBitwiseExpression (Context context, BrygParser.ExpressionContext leftCtx,
                                       BrygParser.ExpressionContext rightCtx) {
        super (context, leftCtx, rightCtx);
        init ();
    }

    protected BinaryBitwiseExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
        init ();
    }

    private void init () {
        Pair<Expression, Expression> result = CoercionUtil.applyBinaryCoercion (context, left, right);
        left = result.a;
        right = result.b;
        setType (left.getType ());

        if (!type.isIntegralType ()) {
            throw new BrygJitException ("Bitwise operands must have integral types (byte, short, int, long).",
                    getLine ());
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        left.compile ();
        right.compile ();
        // -> T, T

        int opcode = type.getAsmType ().getOpcode (getOpcode ());
        mv.visitInsn (opcode);
        // T, T -> T
    }

    /**
     * @return The opcode for the bitwise expression in the integer (IAND, IOR, IXOR) form.
     */
    protected abstract int getOpcode ();

}
