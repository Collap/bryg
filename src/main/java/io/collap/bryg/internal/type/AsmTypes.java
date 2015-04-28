package io.collap.bryg.internal.type;

import bryg.org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AsmTypes {

    private static final Map<Class<?>, Type> asmTypeCache = new HashMap<>();

    public static synchronized Type getAsmType(Class<?> cl) {
        @Nullable Type cachedType = asmTypeCache.get(cl);
        if (cachedType == null) {
            cachedType = Type.getType(cl);
            asmTypeCache.put(cl, cachedType);
        }
        return cachedType;
    }

}
