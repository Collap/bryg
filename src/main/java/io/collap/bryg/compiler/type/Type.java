package io.collap.bryg.compiler.type;

import java.util.ArrayList;
import java.util.List;

public class Type {

    private Class<?> javaType;
    private org.objectweb.asm.Type asmType;
    private List<Type> genericTypes; /* Lazily loaded. */

    public Type (Class<?> javaType) {
        this.javaType = javaType;
        this.asmType = AsmTypes.getAsmType (javaType);
    }

    public Class<?> getJavaType () {
        return javaType;
    }

    public org.objectweb.asm.Type getAsmType () {
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

}
