package io.collap.bryg.internal.module;

import io.collap.bryg.module.Function;
import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.internal.compiler.Context;

public abstract class BlockFunction extends Function {

    @Override
    public void compile(Context context, FunctionCallExpression call) {
        enter(context, call);
        call.getStatementOrBlock().compile();
        exit(context, call);
    }

    public abstract void enter(Context context, FunctionCallExpression call);

    public abstract void exit(Context context, FunctionCallExpression call);

}
