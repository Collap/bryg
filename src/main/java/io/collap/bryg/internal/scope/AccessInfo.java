package io.collap.bryg.internal.scope;

import io.collap.bryg.internal.compiler.ast.expression.Expression;

import javax.annotation.Nullable;

public class AccessInfo {

    public static enum Mode {
        get,
        set
    }

    private Mode mode;
    private @Nullable Expression expression;

}
