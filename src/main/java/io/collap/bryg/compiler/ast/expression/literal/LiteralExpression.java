package io.collap.bryg.compiler.ast.expression.literal;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.exception.BrygJitException;

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
