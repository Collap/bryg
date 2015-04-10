package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;

public class FieldInfo extends VariableInfo {

    public FieldInfo(VariableInfo variableInfo) {
        this(variableInfo.getType(), variableInfo.getName(), variableInfo.getMutability(), variableInfo.getNullness());
    }

    public FieldInfo(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

}
