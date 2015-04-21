package io.collap.bryg.internal.type;

import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class TypeHelper {

    public static String generateMethodDesc(@Nullable Class<?>[] argumentClasses, Class<?> returnClass) {
        @Nullable Type[] types = null;
        if (argumentClasses != null) {
            types = new Type[argumentClasses.length];
            for (int i = 0; i < argumentClasses.length; ++i) {
                types[i] = Types.fromClass(argumentClasses[i]);
            }
        }
        return generateMethodDesc(types, Types.fromClass(returnClass));
    }

    public static String generateMethodDesc(@Nullable Type[] argumentTypes, Type returnType) {
        @Nullable String[] descs = null;
        if (argumentTypes != null) {
            descs = new String[argumentTypes.length];
            for (int i = 0; i < argumentTypes.length; i++) {
                descs[i] = argumentTypes[i].getDescriptor();
            }
        }
        return generateMethodDesc(descs, returnType.getDescriptor());
    }

    public static String generateMethodDesc(@Nullable String[] argumentDescs, String returnDesc) {
        StringBuilder builder = new StringBuilder(32);
        builder.append('(');
        if (argumentDescs != null) {
            for (String desc : argumentDescs) {
                builder.append(desc);
            }
        }
        builder.append(')');
        builder.append(returnDesc);
        return builder.toString();
    }

    public static String toInternalName(String fullName) {
        return fullName.replace('.', '/');
    }

    public static Field findField(Type type, int line, String fieldName) throws NoSuchFieldException {
        if (!(type instanceof CompiledType)) {
            throw new BrygJitException("Can't get a Java field from a non-Java type.", line);
        }
        return findField(((CompiledType) type).getJavaType(), fieldName);
    }

    public static Field findField(Class<?> cl, String fieldName) throws NoSuchFieldException {
        // TODO: This looks ugly. Can we check this without exceptions?
        @Nullable Field field = null;
        boolean successful = false;
        while (!successful) {
            try {
                field = cl.getDeclaredField(fieldName);
                successful = true;
            } catch (NoSuchFieldException ex) {
                cl = cl.getSuperclass();
                if (cl == null) { // This CAN actually be null here.
                    throw ex;
                }
            }
        }
        return field;
    }

}
