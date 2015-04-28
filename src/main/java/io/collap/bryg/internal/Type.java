package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class Type {

    protected @Nullable List<Type> genericTypes;

    /**
     * @return An <b>immutable</b> list of generic types.
     */
    public List<Type> getGenericTypes() {
        if (genericTypes == null) {
            return Collections.emptyList();
        } else {
            return genericTypes;
        }
    }

    /**
     * Makes the list immutable.
     */
    public void setGenericTypes(List<Type> types) {
        if (genericTypes != null) {
            throw new IllegalStateException("Generic types are immutable and must not be overwritten.");
        }
        genericTypes = Collections.unmodifiableList(types);
    }

    public abstract String getInternalName();

    public abstract String getDescriptor();

    public abstract int getOpcode(int ix);

    /**
     * @return Whether the type has the same Java base type as this type.
     */
    public abstract boolean similarTo(Type type);

    /**
     * @return Whether the class is the same as the Java base type of this type.
     */
    public abstract boolean similarTo(Class<?> type);

    public abstract boolean isPrimitive();

    public abstract boolean isIntegralType();

    public abstract boolean isFloatingPointType();

    public boolean isNumeric() {
        return isIntegralType() || isFloatingPointType();
    }

    public boolean is64Bit() {
        return getStackSize() == 2;
    }

    public boolean isUnitType() {
        return false;
    }

    public abstract int getStackSize();

    public abstract boolean isAssignableFrom(Type type);

    public abstract boolean isWrapperType();

    /**
     * @return If this type is a primitive type, the wrapper type that belongs to it, otherwise null.
     */
    public abstract @Nullable Type getWrapperType();

    /**
     * @return If this type is a wrapper type, the primitive type that belongs to it, otherwise null.
     */
    public abstract @Nullable Type getPrimitiveType();

    public abstract boolean isInterface();

}
