package io.collap.bryg.internal.compiler.ast.expression.arithmetic;

import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
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

    protected BinaryArithmeticExpression(CompilationContext compilationContext, BrygParser.ExpressionContext leftCtx,
                                         BrygParser.ExpressionContext rightCtx) {
        super(compilationContext, leftCtx, rightCtx);
        setupType();
    }

    protected BinaryArithmeticExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, left, right, line);
        setupType();
    }

    protected void setupType() {
        Pair<Expression, Expression> result = CoercionUtil.applyBinaryCoercion(compilationContext, left, right);
        left = result.a;
        right = result.b;
        setType(left.getType());
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();
        Type typeNonNull = getType();
        if (typeNonNull.isPrimitive()) {
            left.compile();
            right.compile();

            int op = typeNonNull.getOpcode(getOpcode());
            mv.visitInsn(op);
        } else {
            throw new BrygJitException("Unexpected type " + type, getLine());
        }
    }

    /**
     * @return The opcode for the arithmetic expression in the integer (IADD, ISUB, etc.) form.
     */
    protected abstract int getOpcode();

}
