package io.collap.bryg.internal.type;

import io.collap.bryg.internal.Type;

import javax.annotation.Nullable;

public class TypeHelper {

    public static String generateMethodDesc (@Nullable Class<?>[] argumentClasses, Class<?> returnClass) {
        @Nullable Type[] types = null;
        if (argumentClasses != null) {
            types = new Type[argumentClasses.length];
            for (int i = 0; i < argumentClasses.length; ++i) {
                types[i] = Types.fromClass (argumentClasses[i]);
            }
        }
        return generateMethodDesc (types, Types.fromClass (returnClass));
    }

    public static String generateMethodDesc (@Nullable Type[] argumentTypes, Type returnType) {
        @Nullable String[] descs = null;
        if (argumentTypes != null) {
            descs = new String[argumentTypes.length];;
            for (int i = 0; i < argumentTypes.length; i++) {
                descs[i] = argumentTypes[i].getDescriptor ();
            }
        }
        return generateMethodDesc (descs, returnType.getDescriptor ());
    }

    public static String generateMethodDesc (@Nullable String[] argumentDescs, String returnDesc) {
        StringBuilder builder = new StringBuilder (32);
        builder.append ('(');
        if (argumentDescs != null) {
            for (String desc : argumentDescs) {
                builder.append (desc);
            }
        }
        builder.append (')');
        builder.append (returnDesc);
        return builder.toString ();
    }

    public static String toInternalName (String fullName) {
        return fullName.replace ('.', '/');
    }

}
