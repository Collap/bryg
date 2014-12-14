package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.exception.BrygJitException;

public class LocalVariable extends Variable {

    public static final int INVALID_ID = -1;

    /**
     * The ID is guaranteed to be set once the nodes are compiled, but not before.
     */
    private int id = INVALID_ID;

    public LocalVariable (Type type, String name, boolean isMutable) {
        super (type, name, isMutable);
    }

    public LocalVariable (Type type, String name, boolean isMutable, boolean isNullable) {
        super (type, name, isMutable, isNullable);
    }

    public int getId () {
        if (id == INVALID_ID) {
            throw new BrygJitException ("The ID of the variable '" + getName () + "' has not been set yet.", -1); // TODO: Line?
        }

        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

}
