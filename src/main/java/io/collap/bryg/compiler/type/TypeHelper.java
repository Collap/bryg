package io.collap.bryg.compiler.type;

import javax.annotation.Nullable;

public class TypeHelper {

    public static String generateMethodDesc (@Nullable Class<?>[] argumentClasses, Class<?> returnClass) {
        Type[] types = null;
        if (argumentClasses != null) {
            types = new Type[argumentClasses.length];
            for (int i = 0; i < argumentClasses.length; ++i) {
                types[i] = new Type (argumentClasses[i]);
            }
        }
        return generateMethodDesc (types, new Type (returnClass));
    }

    public static String generateMethodDesc (@Nullable Type[] argumentTypes, Type returnType) {
        StringBuilder builder = new StringBuilder (32);
        builder.append ('(');
        if (argumentTypes != null) {
            for (Type type : argumentTypes) {
                builder.append (type.getAsmType ().getDescriptor ());
            }
        }
        builder.append (')');
        builder.append (returnType.getAsmType ().getDescriptor ());
        return builder.toString ();
    }

}
