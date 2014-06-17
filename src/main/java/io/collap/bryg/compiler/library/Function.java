package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.expression.Type;

public interface Function {

    /**
     * @return null indicates that the function has the void return type.
     */
    public void compile (RenderVisitor visitor, FunctionCallExpression call);

    public Type getReturnType ();

}
