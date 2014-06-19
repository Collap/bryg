package io.collap.bryg.compiler.type;

import org.objectweb.asm.Type;

import javax.annotation.Nullable;

public class TypeHelper {

    public static String generateMethodDesc (@Nullable Class<?>[] argumentTypes, Class<?> returnType) {
        StringBuilder builder = new StringBuilder (32);
        builder.append ('(');
        if (argumentTypes != null) {
            for (Class<?> type : argumentTypes) {
                builder.append (Type.getType (type).getDescriptor ());
            }
        }
        builder.append (')');
        builder.append (Type.getType (returnType).getDescriptor ());
        return builder.toString ();
    }

}
