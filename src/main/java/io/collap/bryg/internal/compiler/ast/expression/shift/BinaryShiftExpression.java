package io.collap.bryg.internal.compiler.ast.expression.shift;

import io.collap.bryg.internal.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryShiftExpression extends BinaryExpression {

    protected BinaryShiftExpression(CompilationContext compilationContext, BrygParser.BinaryShiftExpressionContext ctx) {
        super(compilationContext, ctx.expression(0), ctx.expression(1));
        init();
    }

    protected BinaryShiftExpression(CompilationContext compilationContext, Expression left, Expression right, int line) {
        super(compilationContext, left, right, line);
        init();
    }

    private void init() {
        if (!right.getType().similarTo(Integer.TYPE)) {
            throw new BrygJitException("The shift amount (right-hand operand) must be an int.", getLine());
        }

        if (!left.getType().isIntegralType()) {
            throw new BrygJitException("The shifted value (left-hand operand) must be an integral.", getLine());
        }

        setType(left.getType());
    }

    @Override
    public void compile() {
        left.compile();
        right.compile();

        int op = getType().getOpcode(getOpcode());
        compilationContext.getMethodVisitor().visitInsn(op);
    }

    /**
     * @return The opcode in integer form (i.e. ISHR, ISHL, IUSHR).
     */
    protected abstract int getOpcode();

}
