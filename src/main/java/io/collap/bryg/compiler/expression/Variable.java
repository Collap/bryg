package io.collap.bryg.compiler.expression;

import io.collap.bryg.compiler.type.Type;

public class Variable {

    // TODO: Handle mutability.

    private Type type;
    private String name;
    private int id;

    public Variable (Type type, String name, int id) {
        this.type = type;
        this.name = name;
        this.id = id;
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

}
