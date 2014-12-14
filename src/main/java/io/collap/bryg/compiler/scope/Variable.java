package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

public abstract class Variable {

    protected VariableInfo info;
    protected int uses;

    public Variable (Type type, String name, boolean isMutable) {
        this (new VariableInfo (type, name, isMutable));
    }

    public Variable (Type type, String name, boolean isMutable, boolean isNullable) {
        this (new VariableInfo (type, name, isMutable, isNullable));
    }

    public Variable (VariableInfo info) {
        this.info = info;
    }

    public VariableInfo getInfo () {
        return info;
    }

    public Type getType () {
        return info.getType ();
    }

    public String getName () {
        return info.getName ();
    }

    public boolean isMutable () {
        return info.isMutable ();
    }

    public boolean isNullable () {
        return info.isNullable ();
    }

    public boolean isUsed () {
        return uses > 0;
    }

    public void incrementUses () {
        ++uses;
    }

}
