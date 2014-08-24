package io.collap.bryg.compiler.library;

import io.collap.bryg.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.compiler.context.Context;

public abstract class BlockFunction implements Function {

    @Override
    public void compile (Context context, FunctionCallExpression call) {
        enter (context, call);
        call.getStatementOrBlock ().compile ();
        exit (context, call);
    }

    public abstract void enter (Context context, FunctionCallExpression call);
    public abstract void exit (Context context, FunctionCallExpression call);

}
