package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

import javax.annotation.Nullable;

public class RootScope extends Scope {

    private int nextId = 0;

    public RootScope () {
        super (null);
    }

    @Override
    public Variable getVariable (String name) {
        return variables.get (name);
    }

    @Override
    public int calculateNextId (@Nullable Type type) {
        int id = nextId;

        /* Double and long use two variable slots. */
        boolean isWide = false;
        if (type != null && type.getJavaType ().isPrimitive ()) {
            isWide = type.equals (Long.TYPE) || type.equals (Double.TYPE);
        }

        if (isWide) {
            nextId += 2;
        }else {
            nextId += 1;
        }

        return id;
    }

}
