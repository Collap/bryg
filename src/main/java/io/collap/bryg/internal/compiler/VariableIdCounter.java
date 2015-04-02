package io.collap.bryg.internal.compiler;

import io.collap.bryg.internal.Type;

import javax.annotation.Nullable;

public class VariableIdCounter {

    int nextId = 0;

    public int next(@Nullable Type type) {
        int id = nextId;
        if (type != null) {
            nextId += type.getStackSize();
        } else {
            nextId += 1;
        }
        return id;
    }

}
