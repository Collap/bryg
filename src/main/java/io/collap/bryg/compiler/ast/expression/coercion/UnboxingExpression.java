package io.collap.bryg.compiler.ast.expression.coercion;

import io.collap.bryg.compiler.ast.expression.Expression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;

import java.util.HashMap;
import java.util.Map;

import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * The child can be a DummyExpression.
 */
public class UnboxingExpression extends Expression {

    private static Map<Class<?>, String> valueMethodNames = new HashMap<> ();

    static {
        valueMethodNames.put (Boolean.class, "booleanValue");
        valueMethodNames.put (Character.class, "charValue");
        valueMethodNames.put (Byte.class, "byteValue");
        valueMethodNames.put (Short.class, "shortValue");
        valueMethodNames.put (Integer.class, "intValue");
        valueMethodNames.put (Long.class, "longValue");
        valueMethodNames.put (Float.class, "floatValue");
        valueMethodNames.put (Double.class, "doubleValue");
    }

    private Expression child;

    public UnboxingExpression (Context context, Expression child, Type unboxedType) {
        super (context);
        setLine (child.getLine ());
        setType (unboxedType);
        this.child = child;
    }

    @Override
    public void compile () {
        Type boxType = child.getType ();
        String boxTypeName = child.getType ().getAsmType ().getInternalName ();
        String valueMethodName = valueMethodNames.get (boxType.getJavaType ());

        child.compile ();
        // -> T

        context.getMethodVisitor ().visitMethodInsn (INVOKEVIRTUAL, boxTypeName, valueMethodName,
                TypeHelper.generateMethodDesc (
                        null,
                        type
                ),
                false
        );
        // T -> primitive
    }

}
