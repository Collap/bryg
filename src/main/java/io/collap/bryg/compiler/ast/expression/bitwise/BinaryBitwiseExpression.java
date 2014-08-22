package io.collap.bryg.compiler.ast.expression.bitwise;

import io.collap.bryg.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryBitwiseExpression extends BinaryExpression {

    protected BinaryBitwiseExpression (StandardVisitor visitor, BrygParser.ExpressionContext leftCtx,
                                       BrygParser.ExpressionContext rightCtx) {
        super (visitor, leftCtx, rightCtx);
        init ();
    }

    protected BinaryBitwiseExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
        init ();
    }

    private void init () {
        if (!left.getType ().isIntegralType () || !right.getType ().isIntegralType ()) {
            throw new BrygJitException ("Bitwise operands must have integral types (byte, short, int, long).",
                    getLine ());
        }

        setType (CoercionHelper.getTargetType (left.getType (), right.getType (), getLine ()));
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = visitor.getMethod ();

        CoercionHelper.attemptBinaryCoercion (mv, left, right, type);
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
