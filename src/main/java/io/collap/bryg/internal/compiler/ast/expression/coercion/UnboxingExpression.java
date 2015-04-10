package io.collap.bryg.internal.compiler.ast.expression.coercion;

import io.collap.bryg.internal.compiler.ast.expression.Expression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.CompiledType;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.BrygJitException;

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

    public UnboxingExpression (CompilationContext compilationContext, Expression child, Type unboxedType) {
        super (compilationContext);
        setLine (child.getLine ());
        setType (unboxedType);
        this.child = child;

        if (!(child.getType () instanceof CompiledType)) {
            throw new BrygJitException ("Can't call a Java method on a non-Java type.", getLine ());
        }

    }

    @Override
    public void compile () {
        Type boxType = child.getType ();
        String boxTypeName = child.getType ().getInternalName ();
        String valueMethodName = valueMethodNames.get (((CompiledType) boxType).getJavaType ());

        child.compile ();
        // -> T

        compilationContext.getMethodVisitor ().visitMethodInsn (INVOKEVIRTUAL, boxTypeName, valueMethodName,
                TypeHelper.generateMethodDesc (
                        null,
                        type
                ),
                false
        );
        // T -> primitive
    }

}
