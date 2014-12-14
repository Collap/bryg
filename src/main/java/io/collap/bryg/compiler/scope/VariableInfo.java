package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

public class VariableInfo {

    protected Type type;
    protected String name;
    protected boolean isMutable;

    /* Currently only set to false for inDeclarations that are not optional
       and variables with primitive types. This could possibly decided for
       immutable variables that are assigned an object as well, but beyond
       that, deciding whether the variable is nullable or not is harder,
       especially with the lacking Java support for such a feature. */
    protected boolean isNullable;

    public VariableInfo (Type type, String name, boolean isMutable) {
        this (type, name, isMutable, !type.isPrimitive ());
    }

    public VariableInfo (Type type, String name, boolean isMutable, boolean isNullable) {
        this.type = type;
        this.name = name;
        this.isMutable = isMutable;
        this.isNullable = isNullable;
    }

    public Type getType () {
        return type;
    }

    public String getName () {
        return name;
    }

    public boolean isMutable () {
        return isMutable;
    }

    public boolean isNullable () {
        return isNullable;
    }

}
