package io.collap.bryg.internal.compiler.ast.expression.bitwise;

import io.collap.bryg.internal.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.CoercionUtil;
import io.collap.bryg.internal.compiler.util.Pair;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryBitwiseExpression extends BinaryExpression {

    protected BinaryBitwiseExpression(CompilationContext compilationContext, BrygParser.ExpressionContext leftCtx,
                                      BrygParser.ExpressionContext rightCtx) {
        super(compilationContext, leftCtx, rightCtx);
        init();
    }

    protected BinaryBitwiseExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, left, right, line);
        init();
    }

    private void init() {
        Pair<Expression, Expression> result = CoercionUtil.applyBinaryCoercion(compilationContext, left, right);
        left = result.a;
        right = result.b;
        setType(left.getType());

        if (!getType().isIntegralType()) {
            throw new BrygJitException("Bitwise operands must have integral types (byte, short, int, long).",
                    getLine());
        }
    }

    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        left.compile();
        right.compile();
        // -> T, T

        int opcode = getType().getOpcode(getOpcode());
        mv.visitInsn(opcode);
        // T, T -> T
    }

    /**
     * @return The opcode for the bitwise expression in the integer (IAND, IOR, IXOR) form.
     */
    protected abstract int getOpcode();

}
