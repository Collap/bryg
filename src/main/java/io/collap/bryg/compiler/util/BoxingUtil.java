package io.collap.bryg.compiler.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;

import java.util.HashMap;
import java.util.Map;

import static bryg.org.objectweb.asm.Opcodes.*;

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

    public static boolean isBoxedType (Type type) {
        return boxToPrimitive.containsKey (type.getJavaType ());
    }

    public static boolean isUnboxedType (Type type) {
        return primitiveToBox.containsKey (type.getJavaType ());
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

    /**
     * Assumes that the value is already on the stack.
     */
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

    /**
     * Compiles the expression.
     */
    public static void compileBoxing (BrygMethodVisitor mv, Expression expression, Type box) {
        String boxTypeName = box.getAsmType ().getInternalName ();
        Type paramType = expression.getType ();

        /* There are no conversions from int to short or byte, so we just need to find the right constructor.
         * This allows to box bytes and shorts from int expressions. */
        if (paramType.similarTo (Integer.TYPE)) {
            if (box.similarTo (Byte.class)) {
                paramType = new Type (Byte.TYPE);
            }else if (box.similarTo (Short.class)) {
                paramType = new Type (Short.TYPE);
            }
        }

        mv.visitTypeInsn (NEW, boxTypeName);
        // -> T

        mv.visitInsn (DUP);
        // T -> T, T

        expression.compile ();
        // -> primitive

        mv.visitMethodInsn (INVOKESPECIAL, boxTypeName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Type[] { paramType },
                        new Type (Void.TYPE)
                ),
                false);
        // T, primitive ->
    }

}
