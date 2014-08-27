package io.collap.bryg.compiler.ast.expression;

import io.collap.bryg.compiler.ast.AccessMode;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static bryg.org.objectweb.asm.Opcodes.*;

public class AccessExpression extends Expression {

    private AccessMode mode;

    private Expression child;

    private Field field;
    private Method getterOrSetter;

    /**
     * The setFieldExpression is compiled with AccessMode.set only.
     * This technique is used to get the order ALOAD, val.compile (), PUTFIELD right without having to SWAP at runtime.
     */
    private Expression setFieldExpression;

    public AccessExpression (Context context, BrygParser.AccessExpressionContext ctx,
                             AccessMode mode) throws NoSuchFieldException {
        this (context, ctx, mode, null);
    }

    public AccessExpression (Context context, BrygParser.AccessExpressionContext ctx,
                             AccessMode mode, Expression setFieldExpression) throws NoSuchFieldException {
        super (context);
        this.mode = mode;
        this.setFieldExpression = setFieldExpression;
        setLine (ctx.getStart ().getLine ());

        String fieldName = IdUtil.idToString (ctx.id ());
        child = (Expression) context.getParseTreeVisitor ().visit (ctx.expression ());
        Type childType = child.getType ();

        if (childType.getJavaType ().isPrimitive ()) {
            throw new BrygJitException ("The expression that is accessed by an access expression must not be a primitive type!"
                + " (Type: " + childType + ")", getLine ());
        }

        field = childType.getJavaType ().getDeclaredField (fieldName);
        if (mode == AccessMode.get) {
            setType (new Type (field.getType ()));
        }else {
            setType (new Type (Void.TYPE));
        }

        if (Modifier.isPublic (field.getModifiers ())) {
            getterOrSetter = null;
        }else {
            String fieldNameCapitalized = fieldName.substring (0, 1).toUpperCase () + fieldName.substring (1);
            if (mode == AccessMode.get) {
                String getterName = "get" + fieldNameCapitalized;
                try {
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
                    System.out.println ("Info: Getter " + getterName + " of type " + field.getType () + " not found.");
                }
            }else if (mode == AccessMode.set) {
                String setterName = "set" + fieldNameCapitalized;
                try {
                    Method localSetter = childType.getJavaType ().getMethod (setterName, field.getType ());
                    if (Modifier.isPublic (localSetter.getModifiers ())) {
                        getterOrSetter = localSetter;
                    } else {
                        System.out.println ("The supposed setter " + setterName + " is not public.");
                    }
                } catch (NoSuchMethodException e) {
                    /* Expected. */
                    System.out.println ("Info: Setter " + setterName + " of type " + field.getType () + " not found.");
                }
            }
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        child.compile ();
        String childInternalName = child.getType ().getAsmType ().getInternalName ();

        if (mode == AccessMode.get) {
            if (getterOrSetter != null) {
                mv.visitMethodInsn (INVOKEVIRTUAL, childInternalName,
                        getterOrSetter.getName (), TypeHelper.generateMethodDesc (
                                null,
                                type
                        ), false);
                // -> T
            }else if (field != null && Modifier.isPublic (field.getModifiers ())) {
                /* Get the field directly. */
                mv.visitFieldInsn (GETFIELD, childInternalName,
                        field.getName (), type.getAsmType ().getDescriptor ());
                // -> T
            }else {
                throw new BrygJitException ("The getter and field for object access are both inaccessible or non-existent!",
                        getLine ());
            }
        }else { /* AccessMode.set */
            if (setFieldExpression == null) {
                throw new BrygJitException ("A setFieldExpression has to be supplied to the constructor for" +
                        "a correct setter order. If you see this expression as a user, this is likely a compiler bug!", getLine ());
            }
            setFieldExpression.compile ();

            Type fieldType = new Type (field.getType ());
            if (getterOrSetter != null) {
                mv.visitMethodInsn (INVOKEVIRTUAL, childInternalName,
                        getterOrSetter.getName (), TypeHelper.generateMethodDesc (
                                new Type[]{fieldType},
                                new Type (Void.TYPE)
                        ), false);
                // T ->
            }else if (field != null) {
                /* Set the field directly. */
                mv.visitFieldInsn (PUTFIELD, childInternalName,
                        field.getName (), fieldType.getAsmType ().getDescriptor ());
                // T ->
            }else {
                throw new BrygJitException ("The setter and field for object access are both inaccessible or non-existent!",
                        getLine ());
            }
        }
    }

    public void setSetFieldExpression (Expression setFieldExpression) {
        this.setFieldExpression = setFieldExpression;
    }

    public Field getField () {
        return field;
    }

}
