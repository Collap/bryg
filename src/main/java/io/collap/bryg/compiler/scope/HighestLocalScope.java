package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

import javax.annotation.Nullable;

public class HighestLocalScope extends Scope {

    private int nextId = 0;

    public HighestLocalScope (UnitScope parent) {
        super (parent);
    }

    @Override
    public int calculateNextId (@Nullable Type type) {
        int id = nextId;

        if (type != null) {
            nextId += type.getStackSize ();
        }else {
            nextId += 1;
        }

        return id;
    }

    public UnitScope getUnitScope () {
        return (UnitScope) parent;
    }

}
