package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.util.TypeHelper;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

public class AccessExpression extends Expression {

    private Expression child;

    private Field field;
    private Method getter;
    private Method setter;

    public AccessExpression (RenderVisitor visitor, BrygParser.AccessExpressionContext ctx) throws NoSuchFieldException {
        super (visitor);

        String fieldName = ctx.Id ().getText ();
        child = (Expression) visitor.visit (ctx.expression ());

        if (child.getType () instanceof PrimitiveType) {
            // TODO: Probably not the most appropriate Exception.
            throw new RuntimeException ("The expression child of an access expression must not be a primitive type!");
        }

        Class<?> expressionClass = ((ClassType) child.getType ()).getActualType ();
        field = expressionClass.getDeclaredField (fieldName);
        Class<?> fieldClass = field.getType ();
        if (fieldClass.isPrimitive ()) {
            setType (PrimitiveType.fromJavaClass (fieldClass));
        }else {
            setType (new ClassType (fieldClass));
        }

        if (Modifier.isPublic (field.getModifiers ())) {
            getter = null;
            setter = null;
        }else {
            /* Search for getter and setter. */
            String fieldNameCapitalized = fieldName.substring (0, 1).toUpperCase () + fieldName.substring (1); // TODO: Inefficient?
            try {
                String getterName = "get" + fieldNameCapitalized;
                Method localGetter = expressionClass.getMethod (getterName);
                if (localGetter.getReturnType ().equals (field.getType ())) {
                    if (Modifier.isPublic (localGetter.getModifiers ())) {
                        getter = localGetter;
                    }else {
                        System.out.println ("The supposed getter " + getterName + " is not public.");
                    }
                }else {
                    System.out.println ("The return type of the supposed getter " + getterName + " does not match the field's type.");
                }
            } catch (NoSuchMethodException e) {
                /* Expected. */
            }

            try {
                String setterName = "set" + fieldNameCapitalized;
                Method localSetter = expressionClass.getMethod (setterName, type.getActualType ());
                if (Modifier.isPublic (localSetter.getModifiers ())) {
                    setter = localSetter;
                }else {
                    System.out.println ("The supposed setter " + setterName + " is not public.");
                }
            } catch (NoSuchMethodException e) {
                /* Expected. */
            }
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        child.compile ();

        /* The following code concerns getters. */
        if (getter != null) {
            method.visitMethodInsn (INVOKEVIRTUAL, ((ClassType) child.getType ()).getJvmName (),
                    getter.getName (), TypeHelper.generateMethodDesc (
                            null,
                            type
                    ), false);
            // -> type
        }else if (field != null) {
            /* Get the field directly. */
            method.visitFieldInsn (GETFIELD, ((ClassType) child.getType ()).getJvmName (),
                field.getName (), type.toDescriptorFormat ());
            // -> type
        }

    }

    // TODO: Handle setters.

}
