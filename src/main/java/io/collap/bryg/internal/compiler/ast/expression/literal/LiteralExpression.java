package io.collap.bryg.internal.compiler.ast.expression.literal;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.BrygJitException;

public abstract class LiteralExpression extends Expression {

    protected Object value;

    protected LiteralExpression (Context context, int line) {
        super (context);
        setLine (line);
    }

    @Override
    public void compile () {
        if (value == null) {
            throw new BrygJitException ("Value of literal has not been set!", getLine ());
        }

        context.getMethodVisitor ().visitLdcInsn (value);

        System.out.println ("BAZINCOMPILE: " + value + " [" + getLine () + "]");
    }

    @Override
    public Object getConstantValue () {
        return value;
    }

}
