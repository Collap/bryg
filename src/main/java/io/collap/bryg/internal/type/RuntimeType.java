package io.collap.bryg.internal.type;

import io.collap.bryg.internal.Type;

/**
 * Reserved for types that are not yet compiled, which means that a class object it not created yet.
 * May not support all methods provided by the interface.
 *
 * This class makes a few assumptions:
 * - All types are classes.
 * - This type is not similar to any class objects.
 * - The class represented by this type is not a wrapper type.
 */
public class RuntimeType extends Type {

    protected String internalName;
    protected String descriptor;
    protected boolean isInterface;

    public RuntimeType (String internalName) {
        this.internalName = internalName;
        this.descriptor = "L" + internalName + ";";
        this.isInterface = false;
    }

    @Override
    public String getInternalName () {
        return internalName;
    }

    @Override
    public String getDescriptor () {
        return descriptor;
    }

    @Override
    public int getOpcode (int ix) {
        return AsmTypes.getAsmType (Object.class).getOpcode (ix);
    }

    @Override
    public boolean similarTo (Type type) {
        return false;
    }

    @Override
    public boolean similarTo (Class<?> type) {
        return false;
    }

    @Override
    public boolean isPrimitive () {
        return false;
    }

    @Override
    public boolean isIntegralType () {
        return false;
    }

    @Override
    public boolean isFloatingPointType () {
        return false;
    }

    @Override
    public int getStackSize () {
        return 1;
    }

    /**
     * @return Since we can't check the class hierarchy for runtime types, this is always false.
     */
    @Override
    public boolean isAssignableFrom (Type type) {
        return false;
    }

    @Override
    public boolean isWrapperType () {
        return false;
    }

    @Override
    public Type getWrapperType () {
        return null;
    }

    @Override
    public Type getPrimitiveType () {
        return null;
    }

    @Override
    public boolean isInterface () {
        return isInterface;
    }

    public void setInterface (boolean isInterface) {
        this.isInterface = isInterface;
    }

    @Override
    public String toString () {
        return descriptor;
    }

}
