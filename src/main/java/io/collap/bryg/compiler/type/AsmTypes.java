package io.collap.bryg.compiler.type;

import bryg.org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class AsmTypes {

    private static final Map<Class<?>, Type> asmTypeCache = new HashMap<> ();

    public static Type getAsmType (Class<?> cl) {
        Type cachedType = asmTypeCache.get (cl);
        if (cachedType == null) {
            cachedType = Type.getType (cl);
            asmTypeCache.put (cl, cachedType);
        }
        return cachedType;
    }

}
