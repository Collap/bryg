package io.collap.bryg.compiler.util;

import io.collap.bryg.compiler.expression.Type;

import javax.annotation.Nullable;

public class TypeHelper {

    public static String generateMethodDesc (@Nullable Type[] argumentTypes, Type returnType) {
        StringBuilder builder = new StringBuilder (64);
        builder.append ('(');
        if (argumentTypes != null) {
            for (Type type : argumentTypes) {
                builder.append (type.toDescriptorFormat ());
            }
        }
        builder.append (')');
        builder.append (returnType.toDescriptorFormat ());
        return builder.toString ();
    }

}
