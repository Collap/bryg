package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.type.Type;

/**
 * An implementation of this interface <b>must</b> be thread-safe.
 */
public interface Function {

    public void compile (StandardVisitor visitor, FunctionCallExpression call);

    public Type getReturnType ();

}
