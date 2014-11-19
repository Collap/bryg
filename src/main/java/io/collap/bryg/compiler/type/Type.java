package io.collap.bryg.compiler.type;

import java.util.ArrayList;
import java.util.List;

public class Type {

    private Class<?> javaType;
    private bryg.org.objectweb.asm.Type asmType;
    private List<Type> genericTypes; /* Lazily loaded. */

    public Type (Class<?> javaType) {
        this.javaType = javaType;
        this.asmType = AsmTypes.getAsmType (javaType);
    }

    public Class<?> getJavaType () {
        return javaType;
    }

    public bryg.org.objectweb.asm.Type getAsmType () {
        return asmType;
    }

    public List<Type> getGenericTypes () {
        if (genericTypes == null) {
            genericTypes = new ArrayList<> ();
        }
        return genericTypes;
    }

    /**
     *  @return Whether the type has the same Java base type as this type.
     */
    public boolean similarTo (Type type) {
        return javaType.equals (type.getJavaType ());
    }

    /**
     * @return Whether the class is the same as the Java base type of this type.
     */
    public boolean similarTo (Class<?> type) {
        return javaType.equals (type);
    }

    public boolean isIntegralType () {
        // Note: Ordered by suspected amount of occurrence.
        return similarTo (Integer.TYPE) || similarTo (Long.TYPE) || similarTo (Byte.TYPE) || similarTo (Short.TYPE);
    }

    public boolean isFloatingPointType () {
        return similarTo (Double.TYPE) || similarTo (Float.TYPE);
    }

    public boolean isNumeric () {
        return isIntegralType () || isFloatingPointType ();
    }

    public boolean is64Bit () {
        return asmType.getSize () == 2;
    }

    public int getStackSize () {
        /* Double and long use two variable slots. */
        boolean isWide = false;
        if (javaType.isPrimitive ()) {
            isWide = similarTo (Long.TYPE) || similarTo (Double.TYPE);
        }

        return isWide ? 2 : 1;
    }

    @Override
    public String toString () {
        return getJavaType ().toString ();
    }

}
