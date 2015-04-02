package io.collap.bryg.internal;

import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.VariableInfo;

import javax.annotation.Nullable;

public class ParameterInfo extends VariableInfo {

    protected @Nullable Object defaultValue;

    public ParameterInfo(Type type, String name, boolean isMutable, boolean isNullable, @Nullable Object defaultValue) {
        super(type, name, isMutable, isNullable);
        this.defaultValue = defaultValue;
    }

    public boolean isOptional() {
        return defaultValue != null;
    }

    public @Nullable Object getDefaultValue() {
        return defaultValue;
    }

}
