package io.collap.bryg.compiler.ast.expression.shift;

import io.collap.bryg.compiler.ast.expression.BinaryExpression;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

public abstract class BinaryShiftExpression extends BinaryExpression {

    protected BinaryShiftExpression (Context context, BrygParser.BinaryShiftExpressionContext ctx) {
        super (context, ctx.expression (0), ctx.expression (1));
        init ();
    }

    protected BinaryShiftExpression (Context context, Expression left, Expression right, int line) {
        super (context, left, right, line);
        init ();
    }

    private void init () {
        if (!right.getType ().equals (Integer.TYPE)) {
            throw new BrygJitException ("The shift amount (right-hand operand) must be an int.", getLine ());
        }

        if (!left.getType ().isIntegralType ()) {
            throw new BrygJitException ("The shifted value (left-hand operand) must be an integral.", getLine ());
        }

        setType (left.getType ());
    }

    @Override
    public void compile () {
        left.compile ();
        right.compile ();

        int op = type.getAsmType ().getOpcode (getOpcode ());
        context.getMethodVisitor ().visitInsn (op);
    }

    /**
     * @return The opcode in integer form (i.e. ISHR, ISHL, IUSHR).
     */
    protected abstract int getOpcode ();

}
