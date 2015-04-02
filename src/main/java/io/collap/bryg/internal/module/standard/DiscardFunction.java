package io.collap.bryg.internal.module.standard;

import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.module.BlockFunction;

import java.util.Collections;
import java.util.List;

/**
 * This function discards the return value, so that no output is printed implicitly.
 */
public class DiscardFunction extends BlockFunction {

    @Override
    public void enter(Context context, FunctionCallExpression call) {
        context.pushDiscard();
    }

    @Override
    public void exit(Context context, FunctionCallExpression call) {
        context.popDiscard();
    }

    @Override
    public List<Type> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public Type getResultType() {
        return Types.fromClass(Void.TYPE);
    }

}
