package io.collap.bryg.internal.type;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.collap.bryg.internal.Type;

/**
 * This is a type where the Java class object is already created.
 */
public class CompiledType extends Type {

    private static BiMap<Class<?>, Class<?>> wrapperToPrimitive = HashBiMap.create();
    private static BiMap<Class<?>, Class<?>> primitiveToWrapper;

    static {
        wrapperToPrimitive.put(Boolean.class, Boolean.TYPE);
        wrapperToPrimitive.put(Character.class, Character.TYPE);
        wrapperToPrimitive.put(Byte.class, Byte.TYPE);
        wrapperToPrimitive.put(Short.class, Short.TYPE);
        wrapperToPrimitive.put(Integer.class, Integer.TYPE);
        wrapperToPrimitive.put(Long.class, Long.TYPE);
        wrapperToPrimitive.put(Float.class, Float.TYPE);
        wrapperToPrimitive.put(Double.class, Double.TYPE);
        primitiveToWrapper = wrapperToPrimitive.inverse();
    }

    private Class<?> javaType;
    private bryg.org.objectweb.asm.Type asmType;

    /**
     * Types.fromClass should be used!
     */
    protected CompiledType(Class<?> javaType) {
        this.javaType = javaType;
        this.asmType = AsmTypes.getAsmType(javaType);
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public bryg.org.objectweb.asm.Type getAsmType() {
        return asmType;
    }

    @Override
    public String getInternalName() {
        if (isPrimitive()) {
            throw new IllegalStateException("The primitive type " + toString() + " does not have an internal name.");
        }
        return asmType.getInternalName();
    }

    @Override
    public String getDescriptor() {
        return asmType.getDescriptor();
    }

    @Override
    public int getOpcode(int ix) {
        return asmType.getOpcode(ix);
    }

    /**
     * @return Whether the type has the same Java base type as this type.
     */
    @Override
    public boolean similarTo(Type type) {
        if (type instanceof CompiledType) {
            return javaType.equals(((CompiledType) type).getJavaType());
        }
        return false;
    }

    /**
     * @return Whether the class is the same as the Java base type of this type.
     */
    @Override
    public boolean similarTo(Class<?> type) {
        return javaType.equals(type);
    }

    @Override
    public boolean isPrimitive() {
        return javaType.isPrimitive();
    }

    @Override
    public boolean isIntegralType() {
        // Note: Ordered by suspected amount of occurrence.
        return similarTo(Integer.TYPE) || similarTo(Long.TYPE) || similarTo(Byte.TYPE) || similarTo(Short.TYPE);
    }

    @Override
    public boolean isFloatingPointType() {
        return similarTo(Double.TYPE) || similarTo(Float.TYPE);
    }

    @Override
    public int getStackSize() {
        /* Double and long use two variable slots. */
        boolean isWide = false;
        if (javaType.isPrimitive()) {
            isWide = similarTo(Long.TYPE) || similarTo(Double.TYPE);
        }

        return isWide ? 2 : 1;
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (!(type instanceof CompiledType)) {
            return false;
        }

        return javaType.isAssignableFrom(((CompiledType) type).getJavaType());
    }

    @Override
    public boolean isWrapperType() {
        return wrapperToPrimitive.containsKey(javaType);
    }

    @Override
    public Type getWrapperType() {
        if (!isPrimitive()) return null;
        return Types.fromClass(primitiveToWrapper.get(javaType));
    }

    @Override
    public Type getPrimitiveType() {
        if (!isWrapperType()) return null;
        return Types.fromClass(wrapperToPrimitive.get(javaType));
    }

    @Override
    public boolean isInterface() {
        return javaType.isInterface();
    }

    @Override
    public String toString() {
        String str = getJavaType().toString();
        for (int i = 0; i < getGenericTypes().size(); ++i) {
            if (i == 0) {
                str += "<";
            }

            str += getGenericTypes().get(i);

            if (i < getGenericTypes().size() - 1) {
                str += ", ";
            } else {
                str += ">";
            }
        }
        return str;
    }

}
