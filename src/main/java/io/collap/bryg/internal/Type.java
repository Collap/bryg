package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class Type {

    // TODO: This lazy loading is ugly.
    protected @Nullable List<Type> genericTypes; /* The list itself is lazily loaded. */

    public List<Type> getGenericTypes () {
        if (genericTypes == null) {
            genericTypes = new ArrayList<> ();
        }
        return genericTypes;
    }

    public abstract String getInternalName ();
    public abstract String getDescriptor ();
    public abstract int getOpcode (int ix);

    /**
     *  @return Whether the type has the same Java base type as this type.
     */
    public abstract boolean similarTo (Type type);

    /**
     * @return Whether the class is the same as the Java base type of this type.
     */
    public abstract boolean similarTo (Class<?> type);

    public abstract boolean isPrimitive ();
    public abstract boolean isIntegralType ();
    public abstract boolean isFloatingPointType ();

    public boolean isNumeric () {
        return isIntegralType () || isFloatingPointType ();
    }

    public boolean is64Bit () {
        return getStackSize () == 2;
    }

    public abstract int getStackSize ();

    public abstract boolean isAssignableFrom (Type type);

    public abstract boolean isWrapperType ();

    /**
     * @return If this type is a primitive type, the wrapper type that belongs to it, otherwise null.
     */
    public abstract @Nullable Type getWrapperType ();

    /**
     * @return If this type is a wrapper type, the primitive type that belongs to it, otherwise null.
     */
    public abstract @Nullable Type getPrimitiveType ();

    public abstract boolean isInterface ();

}
