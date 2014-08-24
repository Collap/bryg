package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;

/**
 * This function discards the return value, so that no output is printed implicitly.
 */
public class DiscardFunction extends BlockFunction {

    @Override
    public void enter (Context context, FunctionCallExpression call) {
        context.pushDiscard ();
    }

    @Override
    public void exit (Context context, FunctionCallExpression call) {
        context.popDiscard ();
    }

    @Override
    public Type getReturnType () {
        return new Type (Void.TYPE);
    }

}
