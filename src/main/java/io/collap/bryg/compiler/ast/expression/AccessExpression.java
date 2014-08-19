package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

public class AccessExpression extends Expression {

    private AccessMode mode;

    private Expression child;

    private Field field;
    private Method getterOrSetter;

    public AccessExpression (StandardVisitor visitor, BrygParser.AccessExpressionContext ctx, AccessMode mode) throws NoSuchFieldException {
        super (visitor);
        this.mode = mode;
        setLine (ctx.getStart ().getLine ());

        String fieldName = IdHelper.idToString (ctx.id ());
        child = (Expression) visitor.visit (ctx.expression ());
        Type childType = child.getType ();

        if (childType.getJavaType ().isPrimitive ()) {
            throw new BrygJitException ("The expression that is accessed by an access expression must not be a primitive type!"
                + " (Type: " + childType + ")", getLine ());
        }

        field = childType.getJavaType ().getDeclaredField (fieldName);
        setType (new Type (field.getType ()));

        if (Modifier.isPublic (field.getModifiers ())) {
            getterOrSetter = null;
        }else {
            String fieldNameCapitalized = fieldName.substring (0, 1).toUpperCase () + fieldName.substring (1);
            if (mode == AccessMode.get) {
                try {
                    String getterName = "get" + fieldNameCapitalized;
                    Method localGetter = childType.getJavaType ().getMethod (getterName);
                    if (localGetter.getReturnType ().equals (field.getType ())) {
                        if (Modifier.isPublic (localGetter.getModifiers ())) {
                            getterOrSetter = localGetter;
                        } else {
                            System.out.println ("The supposed getter " + getterName + " is not public.");
                        }
                    } else {
                        System.out.println ("The return type of the supposed getter " + getterName + " does not match the field's type.");
                    }
                } catch (NoSuchMethodException e) {
                    /* Expected. */
                }
            }else if (mode == AccessMode.set) {
                try {
                    String setterName = "set" + fieldNameCapitalized;
                    Method localSetter = childType.getJavaType ().getMethod (setterName, type.getJavaType ());
                    if (Modifier.isPublic (localSetter.getModifiers ())) {
                        getterOrSetter = localSetter;
                    } else {
                        System.out.println ("The supposed setter " + setterName + " is not public.");
                    }
                } catch (NoSuchMethodException e) {
                    /* Expected. */
                }
            }
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor method = visitor.getMethod ();

        child.compile ();
        String childInternalName = child.getType ().getAsmType ().getInternalName ();

        if (mode == AccessMode.get) {
            if (getterOrSetter != null) {
                method.visitMethodInsn (INVOKEVIRTUAL, childInternalName,
                        getterOrSetter.getName (), TypeHelper.generateMethodDesc (
                                null,
                                type
                        ), false);
                // -> T
            }else if (field != null) {
            /* Get the field directly. */
                method.visitFieldInsn (GETFIELD, childInternalName,
                        field.getName (), type.getAsmType ().getDescriptor ());
                // -> T
            }else {
                throw new BrygJitException ("The getter and field for object access are both inaccessible or non-existent!",
                        getLine ());
            }
        }else {
            throw new UnsupportedOperationException ("Currently only the 'get' access mode is supported! (Line: " + getLine () + ")");
        }
    }

    // TODO: Handle setters.

}
