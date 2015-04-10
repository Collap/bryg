package io.collap.bryg.internal.module;

import io.collap.bryg.module.Function;
import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;
import io.collap.bryg.internal.compiler.CompilationContext;

public abstract class BlockFunction extends Function {

    @Override
    public void compile(CompilationContext compilationContext, FunctionCallExpression call) {
        enter(compilationContext, call);
        call.getStatementOrBlock().compile();
        exit(compilationContext, call);
    }

    public abstract void enter(CompilationContext compilationContext, FunctionCallExpression call);

    public abstract void exit(CompilationContext compilationContext, FunctionCallExpression call);

}
