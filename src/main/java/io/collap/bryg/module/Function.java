package io.collap.bryg.module;

import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;

import java.util.List;

public abstract class Function implements Member<FunctionCallExpression> {

    protected String name;

    public Function(String name) {
        this.name = name;
    }

    /**
     * TODO: Comment.
     */
    public abstract List<ParameterInfo> getParameters();

    @Override
    public String getName() {
        return name;
    }

}
