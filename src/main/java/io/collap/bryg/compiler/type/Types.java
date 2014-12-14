package io.collap.bryg.compiler.type;

import java.util.HashMap;
import java.util.Map;

public class Types {

    private static Map<Class<?>, Type> typeCache = new HashMap<> ();

    public static synchronized Type fromClass (Class<?> cl) {
        Type type = typeCache.get (cl);
        if (type == null) {
            type = new CompiledType (cl);
            typeCache.put (cl, type);
        }
        return type;
    }

}
