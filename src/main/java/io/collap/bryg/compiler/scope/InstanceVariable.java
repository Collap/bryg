package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

/**
 * This is explicitly an instance variable of the <b>current template or closure</b>.
 */
public class InstanceVariable extends Variable {

    public InstanceVariable (Type type, String name, boolean isMutable) {
        super (type, name, isMutable);
    }

    public InstanceVariable (Type type, String name, boolean isMutable, boolean isNullable) {
        super (type, name, isMutable, isNullable);
    }

}
