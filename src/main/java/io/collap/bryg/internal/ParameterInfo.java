package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;

public class ParameterInfo extends VariableInfo {

    protected @Nullable Object defaultValue; // TODO: Change this into an expression. Would be more fitting...
                                             // Tentative example: List<X> list = new ArrayList<>()

    /**
     *
     */
    protected boolean isImplicit;

    public ParameterInfo(Type type, String name, Mutability mutability, Nullness nullness,
                         @Nullable Object defaultValue) {
        super(type, name, mutability, nullness);
        this.defaultValue = defaultValue;
        this.isImplicit = false;
    }

    /**
     * Creates a copy of the variable info.
     */
    public ParameterInfo(VariableInfo original, @Nullable Object defaultValue) {
        this(original.getType(), original.getName(), original.getMutability(), original.getNullness(), defaultValue);
    }

    public ParameterInfo(Type type, String name, Mutability mutability, Nullness nullness,
                         @Nullable Object defaultValue, boolean isImplicit) {
        super(type, name, mutability, nullness);
        this.defaultValue = defaultValue;
        this.isImplicit = isImplicit;

        if (isImplicit && !(Types.fromClass(Closure.class).isAssignableFrom(type)
                            || type instanceof ClosureInterfaceType)) {
            throw new BrygJitException("An implicit parameter must be a Closure.", Node.UNKNOWN_LINE);
        }
    }

    public boolean isOptional() {
        return defaultValue != null;
    }

    public @Nullable Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isImplicit() {
        return isImplicit;
    }

}
