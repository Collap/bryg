package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.MemberFunctionCallInfo;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.module.MemberFunction;

import java.io.PrintStream;

public class MemberFunctionCallExpression extends Expression {

    private MemberFunction memberFunction;
    private MemberFunctionCallInfo callInfo;

    public MemberFunctionCallExpression(CompilationContext compilationContext, int line, MemberFunction memberFunction,
                                        MemberFunctionCallInfo callInfo) {
        super(compilationContext, line);
        setType(memberFunction.getResultType());
        this.memberFunction = memberFunction;
        this.callInfo = callInfo;
    }

    @Override
    public void compile() {
        memberFunction.compile(compilationContext, callInfo);
    }

    @Override
    public void print(PrintStream out, int depth) {
        super.print(out, depth);
        if (callInfo.getStatementOrBlock() != null) {
            callInfo.getStatementOrBlock().print(out, depth + 1);
        }
    }

    public MemberFunction getMemberFunction() {
        return memberFunction;
    }

    public MemberFunctionCallInfo getCallInfo() {
        return callInfo;
    }

}
