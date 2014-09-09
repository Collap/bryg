package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

public class Variable {

    private Type type;
    private String name;
    private int id;
    private boolean isMutable;

    public Variable (Type type, String name, int id, boolean isMutable) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.isMutable = isMutable;
    }

    public Type getType () {
        return type;
    }

    public String getName () {
        return name;
    }

    public int getId () {
        return id;
    }

    public boolean isMutable () {
        return isMutable;
    }

}
