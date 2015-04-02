package io.collap.bryg.internal.compiler.ast.expression.arithmetic;

import io.collap.bryg.internal.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.Pair;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

// TODO: Evaluate constant expressions more efficiently.

/**
 * The 'left' expression is allowed to be a DummyExpression.
 * The 'right' expression is allowed to be a DummyExpression, if the 'left' expression is one as well.
 */
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
        Pair<Expression, Expression> result = CoercionUtil.applyBinaryCoercion (context, left, right);
        left = result.a;
        right = result.b;
        setType (left.getType ());
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        if (type.isPrimitive ()) {
            left.compile ();
            right.compile ();

            int op = type.getOpcode (getOpcode ());
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
