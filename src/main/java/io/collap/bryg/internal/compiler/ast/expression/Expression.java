package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;

import javax.annotation.Nullable;

public abstract class Expression extends Node {

    /**
     * The type must be set up <b>in the constructor</b>.
     */
    protected @Nullable Type type;

    protected Expression(CompilationContext compilationContext, int line) {
        super(compilationContext, line);
    }

    public Type getType() {
        if (type == null) {
            throw new IllegalStateException("Result type of expression not set.");
        }

        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isConstant() {
        return getConstantValue() != null;
    }

    /**
     * @return If not null, the type of the value is guaranteed to be an instance of the type returned by {@link #getType()}.
     */
    public @Nullable Object getConstantValue() {
        return null;
    }

}
