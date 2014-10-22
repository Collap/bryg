package io.collap.bryg.unit;

import io.collap.bryg.compiler.type.Type;

public final class ParameterInfo {

    private final String name;
    private final Type type;
    private final boolean optional;

    public ParameterInfo (String name, Type type, boolean optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public String getName () {
        return name;
    }

    public Type getType () {
        return type;
    }

    public boolean isOptional () {
        return optional;
    }

}
