package io.collap.bryg.internal.module;

import io.collap.bryg.internal.MemberFunctionCallInfo;
import io.collap.bryg.module.MemberFunction;
import io.collap.bryg.internal.compiler.CompilationContext;

public abstract class BlockFunction extends MemberFunction {

    public BlockFunction(String name) {
        super(name);
    }

    @Override
    public void compile(CompilationContext compilationContext, MemberFunctionCallInfo callInfo) {
        enter(compilationContext, callInfo);
        if (callInfo.getStatementOrBlock() != null) {
            callInfo.getStatementOrBlock().compile();
        }
        exit(compilationContext, callInfo);
    }

    public abstract void enter(CompilationContext compilationContext, MemberFunctionCallInfo callInfo);

    public abstract void exit(CompilationContext compilationContext, MemberFunctionCallInfo callInfo);

}
