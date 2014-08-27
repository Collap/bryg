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
     * @param obj May be a Type or a Class.
     */
    // TODO: Allowing objects of type class is REALLY bending the definition of "equals".
    @Override
    public boolean equals (Object obj) {
        if (obj instanceof Type) {
            return javaType.equals (((Type) obj).getJavaType ());
        }else if (obj instanceof Class<?>) {
            return javaType.equals (obj);
        }
        return false;
    }

    public boolean isIntegralType () {
        // Note: Ordered by suspected amount of occurrence.
        return equals (Integer.TYPE) || equals (Long.TYPE) || equals (Byte.TYPE) || equals (Short.TYPE);
    }

    public boolean isFloatingPointType () {
        return equals (Double.TYPE) || equals (Float.TYPE);
    }

    public boolean isNumeric () {
        return isIntegralType () || isFloatingPointType ();
    }

    public boolean is64Bit () {
        return asmType.getSize () == 2;
    }

    @Override
    public String toString () {
        return getJavaType ().toString ();
    }

}
