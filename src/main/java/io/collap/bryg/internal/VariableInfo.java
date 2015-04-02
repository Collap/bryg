package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;

public class VariableInfo {

    protected Type type;
    protected String name;
    protected Mutability mutability;
    protected Nullness nullness;

    public VariableInfo(Type type, String name, Mutability mutability, Nullness nullness) {
        this.type = type;
        this.name = name;
        this.mutability = mutability;
        this.nullness = nullness;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Mutability getMutability() {
        return mutability;
    }

    public Nullness getNullness() {
        return nullness;
    }

}
