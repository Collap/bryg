package io.collap.bryg.internal;

import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.Expression;

import javax.annotation.Nullable;

public class VariableUsageInfo {

    private static final VariableUsageInfo GET_USAGE = new VariableUsageInfo(AccessMode.get, null);

    private AccessMode accessMode;
    private @Nullable Expression rightExpression;

    public static VariableUsageInfo withGetMode() {
        return GET_USAGE;
    }

    public static VariableUsageInfo withSetMode(Expression rightExpression) {
        return new VariableUsageInfo(AccessMode.set, rightExpression);
    }

    private VariableUsageInfo(AccessMode accessMode, @Nullable Expression rightExpression) {
        this.accessMode = accessMode;
        this.rightExpression = rightExpression;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public Expression getRightExpression() {
        if (accessMode != AccessMode.set) {
            throw new IllegalStateException("The right expression may only be used when the mode is 'set'.");
        }

        if (rightExpression == null) {
            throw new IllegalStateException("Despite the mode being 'set', the right expression is null.");
        }

        return rightExpression;
    }

}
