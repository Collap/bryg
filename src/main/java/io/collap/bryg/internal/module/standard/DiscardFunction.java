package io.collap.bryg.internal.module.standard;

import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.module.BlockFunction;

import java.util.Collections;
import java.util.List;

/**
 * This function discards the return value, so that no output is printed implicitly.
 *
 * This function registers a counter state with the compilation context. If the counter
 * is greater than 1, any print output is discarded. This is a counter instead of a boolean,
 * because 'discard' functions can be nested.
 */
public class DiscardFunction extends BlockFunction {

    @Override
    public void enter(CompilationContext compilationContext, FunctionCallExpression call) {
        compilationContext.pushDiscard();
    }

    @Override
    public void exit(CompilationContext compilationContext, FunctionCallExpression call) {
        compilationContext.popDiscard();
    }

    @Override
    public List<ParameterInfo> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public Type getResultType() {
        return Types.fromClass(Void.TYPE);
    }

}
