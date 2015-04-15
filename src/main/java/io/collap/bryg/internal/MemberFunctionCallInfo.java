package io.collap.bryg.internal;

import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.expression.ArgumentExpression;

import javax.annotation.Nullable;
import java.util.List;

public class MemberFunctionCallInfo {

    private List<ArgumentExpression> argumentExpressions;
    private @Nullable Node statementOrBlock;

    public MemberFunctionCallInfo(List<ArgumentExpression> argumentExpressions) {
        this(argumentExpressions, null);
    }

    public MemberFunctionCallInfo(List<ArgumentExpression> argumentExpressions, @Nullable Node statementOrBlock) {
        this.argumentExpressions = argumentExpressions;
        this.statementOrBlock = statementOrBlock;
    }

    public List<ArgumentExpression> getArgumentExpressions() {
        return argumentExpressions;
    }

    public @Nullable Node getStatementOrBlock() {
        return statementOrBlock;
    }

}
