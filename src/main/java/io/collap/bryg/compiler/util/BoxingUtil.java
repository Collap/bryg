package io.collap.bryg.compiler.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;

import java.util.HashMap;
import java.util.Map;

import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class BoxingUtil {

    private static BiMap<Class<?>, Class<?>> boxToPrimitive = HashBiMap.create ();
    private static BiMap<Class<?>, Class<?>> primitiveToBox;
    private static Map<Class<?>, String> valueMethodNames = new HashMap<> ();

    static {
        boxToPrimitive.put (Boolean.class, Boolean.TYPE);
        boxToPrimitive.put (Character.class, Character.TYPE);
        boxToPrimitive.put (Byte.class, Byte.TYPE);
        boxToPrimitive.put (Short.class, Short.TYPE);
        boxToPrimitive.put (Integer.class, Integer.TYPE);
        boxToPrimitive.put (Long.class, Long.TYPE);
        boxToPrimitive.put (Float.class, Float.TYPE);
        boxToPrimitive.put (Double.class, Double.TYPE);
        primitiveToBox = boxToPrimitive.inverse ();

        valueMethodNames.put (Boolean.class, "booleanValue");
        valueMethodNames.put (Character.class, "charValue");
        valueMethodNames.put (Byte.class, "byteValue");
        valueMethodNames.put (Short.class, "shortValue");
        valueMethodNames.put (Integer.class, "intValue");
        valueMethodNames.put (Long.class, "longValue");
        valueMethodNames.put (Float.class, "floatValue");
        valueMethodNames.put (Double.class, "doubleValue");
    }


    /**
     * @return A primitive type corresponding to 'type' or null.
     */
    public static Type unboxType (Type type) {
        Class<?> javaType = boxToPrimitive.get (type.getJavaType ());

        if (javaType != null) {
            return new Type (javaType);
        }

        return null;
    }

    /**
     * @return A class type corresponding to the primitive type 'type' or null.
     */
    public static Type boxType (Type type) {
        Class<?> javaType = primitiveToBox.get (type.getJavaType ());

        if (javaType != null) {
            return new Type (javaType);
        }

        return null;
    }

    public static void compileUnboxing (BrygMethodVisitor mv, Type box, Type target) {
        String boxTypeName = box.getAsmType ().getInternalName ();
        String valueMethodName = valueMethodNames.get (box.getJavaType ());

        mv.visitMethodInsn (INVOKEVIRTUAL, boxTypeName, valueMethodName,
                TypeHelper.generateMethodDesc (
                        null,
                        target
                ),
                false
        );
        // T -> primitive
    }


}
