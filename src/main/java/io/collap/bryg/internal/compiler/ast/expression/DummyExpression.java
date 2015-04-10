package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;

/**
 * This class can be used to represent an expression which value is already placed on the stack
 * for some obscure reason. The DummyExpression saves the type, but the compile() method does nothing.
 *
 * A DummyExpression should only be used where it is <b>explicitly</b> allowed.
 *
 * // TODO: A DummyExpression is a hack that smells of poor design. Try to replace the instances where it is used with proper expressions.
 */
public class DummyExpression extends Expression {

    public DummyExpression (CompilationContext compilationContext, Type type, int line) {
        super (compilationContext);
        setType (type);
        setLine (line);
    }

    @Override
    public void compile () {

    }

}
