package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;

/**
 * This class can be used to represent an expression which value is already placed on the stack
 * for some obscure reason. The DummyExpression saves the type, but the compile() method does nothing.
 */
public class DummyExpression extends Expression {

    public DummyExpression (Context context, Type type, int line) {
        super (context);
        setType (type);
        setLine (line);
    }

    @Override
    public void compile () {

    }

}
