package io.collap.bryg;

import io.collap.bryg.compiler.type.Type;

public class ParameterInfo {

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

}
