package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;

public interface Function {

    /**
     * @return null indicates that the function has the void return type.
     */
    public void compile (StandardVisitor visitor, FunctionCallExpression call);

    public Class<?> getReturnType ();

}
