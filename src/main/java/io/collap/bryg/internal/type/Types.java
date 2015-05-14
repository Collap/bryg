package io.collap.bryg.internal.type;

import io.collap.bryg.internal.*;

import javax.annotation.Nullable;
import java.util.*;

public class Types {

    private static Map<Class<?>, Type> typeCache = new HashMap<>();

    public static synchronized Type fromClass(Class<?> cl) {
        return fromClass(cl, false);
    }

    /**
     * This method caches the type if generics are not enabled, to optimize memory usage.
     */
    public static synchronized Type fromClass(Class<?> cl, boolean hasGenerics) {
        if (hasGenerics) {
            return new CompiledType(cl);
        } else {
            @Nullable Type type = typeCache.get(cl);
            if (type == null) {
                type = new CompiledType(cl);

                // Set an empty list so that the generic types are not accidentally changed,
                // which would lead to a lot of problems with different generics for the same type.
                type.setGenericTypes(Collections.emptyList());

                typeCache.put(cl, type);
            }
            return type;
        }
    }

}
