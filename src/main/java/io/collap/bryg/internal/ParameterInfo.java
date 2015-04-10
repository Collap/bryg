package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;

import javax.annotation.Nullable;

public class ParameterInfo extends VariableInfo {

    protected @Nullable Object defaultValue; // TODO: Change this into an expression. Would be more fitting...
                                             // Tentative example: List<X> list = new ArrayList<>()

    public ParameterInfo(Type type, String name, Mutability mutability, Nullness nullness,
                         @Nullable Object defaultValue) {
        super(type, name, mutability, nullness);
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a copy of the variable info.
     */
    public ParameterInfo(VariableInfo original, @Nullable Object defaultValue) {
        this(original.getType(), original.getName(), original.getMutability(), original.getNullness(), defaultValue);
    }

    public boolean isOptional() {
        return defaultValue != null;
    }

    public @Nullable Object getDefaultValue() {
        return defaultValue;
    }

}
