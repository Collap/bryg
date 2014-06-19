package io.collap.bryg.compiler.type;

import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class Types {

    private static final Map<Class<?>, Type> asmTypeCache = new HashMap<> ();

    public static Type getAsmType (Class<?> cl) {
        Type cachedType = asmTypeCache.get (cl);
        if (cachedType == null) {
            cachedType = Type.getType (cl);
            asmTypeCache.put (cl, cachedType);
        }
        return cachedType;
    }

    private static final Map<String, Class<?>> typeNameToPrimitiveType = new HashMap<> ();

    static {
        typeNameToPrimitiveType.put ("void", Void.TYPE);
        typeNameToPrimitiveType.put ("boolean", Boolean.TYPE);
        typeNameToPrimitiveType.put ("char", Character.TYPE);
        typeNameToPrimitiveType.put ("byte", Byte.TYPE);
        typeNameToPrimitiveType.put ("short", Short.TYPE);
        typeNameToPrimitiveType.put ("int", Integer.TYPE);
        typeNameToPrimitiveType.put ("long", Long.TYPE);
        typeNameToPrimitiveType.put ("float", Float.TYPE);
        typeNameToPrimitiveType.put ("double", Double.TYPE);
    }

    /**
     * @return The primitive type associated with the type name or null if no association has been found.
     */
    public static Class<?> getPrimitiveTypeFromName (String typeName) {
        return typeNameToPrimitiveType.get (typeName);
    }

}
