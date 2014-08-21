package io.collap.bryg.compiler.ast.expression.arithmetic;

import io.collap.bryg.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.helper.CoercionHelper;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

// TODO: Evaluate constant expressions more efficiently.

public abstract class BinaryArithmeticExpression extends BinaryExpression {


    protected BinaryArithmeticExpression (StandardVisitor visitor, BrygParser.ExpressionContext leftCtx,
                                          BrygParser.ExpressionContext rightCtx) {
        super (visitor, leftCtx, rightCtx);
        setupType ();
    }

    protected BinaryArithmeticExpression (StandardVisitor visitor, int line) {
        super (visitor, line);
        setupType ();
    }

    protected BinaryArithmeticExpression (StandardVisitor visitor, Expression left, Expression right, int line) {
        super (visitor, left, right, line);
        setupType ();
    }

    protected void setupType () {
        setType (CoercionHelper.getTargetType (left.getType (), right.getType (), getLine ()));
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();
        if (type.getJavaType ().isPrimitive ()) {
            CoercionHelper.attemptBinaryCoercion (method, left, right, type);
            int op = type.getAsmType ().getOpcode (getOpcode ());
            method.visitInsn (op);
        }else {
            throw new BrygJitException ("Unexpected type " + type, getLine ());
        }
    }

    /**
     * @return The opcode for the arithmetic expression in the integer (IADD, ISUB, etc.) form.
     */
    protected abstract int getOpcode ();

}
