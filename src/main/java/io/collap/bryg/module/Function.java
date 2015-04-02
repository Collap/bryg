package io.collap.bryg.module;

import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.compiler.ast.expression.FunctionCallExpression;

import java.util.List;

public abstract class Function implements Member<FunctionCallExpression> {

    /**
     * TODO: Comment.
     */
    public abstract List<ParameterInfo> getParameters();

}
