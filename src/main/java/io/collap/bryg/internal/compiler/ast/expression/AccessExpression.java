package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.type.CompiledType;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.parser.BrygParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static bryg.org.objectweb.asm.Opcodes.*;

public class AccessExpression extends Expression {

    private AccessMode mode;

    private Expression child;
    private CompiledType childType;

    private Field field;
    private Method getterOrSetter;

    /**
     * The setFieldExpression is compiled with AccessMode.set only.
     * This technique is used to get the order ALOAD, val.compile (), PUTFIELD right without having to SWAP at runtime.
     */
    private Expression setFieldExpression;

    public AccessExpression (CompilationContext compilationContext, BrygParser.AccessExpressionContext ctx,
                             AccessMode mode) throws NoSuchFieldException {
        this (compilationContext, ctx, mode, null);
    }

    public AccessExpression (CompilationContext compilationContext, BrygParser.AccessExpressionContext ctx,
                             AccessMode mode, Expression setFieldExpression) throws NoSuchFieldException {
        super (compilationContext);
        this.mode = mode;
        this.setFieldExpression = setFieldExpression;
        setLine (ctx.getStart ().getLine ());

        String fieldName = IdUtil.idToString (ctx.id ());
        child = (Expression) compilationContext.getParseTreeVisitor ().visit (ctx.expression ());

        if (!(child.getType () instanceof CompiledType)) {
            throw new BrygJitException ("Can't call a Java method on a non-Java type.", getLine ());
        }

        childType = ((CompiledType) child.getType ());

        if (childType.isPrimitive ()) {
            throw new BrygJitException ("The expression that is accessed by an access expression must not be a primitive type!"
                + " (Type: " + childType + ")", getLine ());
        }

        /* Get field of this class or superclass. */
        Class<?> javaType = childType.getJavaType ();
        boolean successful = false;
        while (!successful) {
            try {
                field = javaType.getDeclaredField (fieldName);
                successful = true;
            } catch (NoSuchFieldException ex) {
                javaType = javaType.getSuperclass ();
                if (javaType == null) {
                    throw ex;
                }
            }
        }

        if (mode == AccessMode.get) {
            setType (Types.fromClass (field.getType ()));
        }else { /* AccessMode.set */
            if (Modifier.isFinal (field.getModifiers ())) {
                throw new BrygJitException ("The field '" + fieldName + "' is final and can not be changed.", getLine ());
            }

            setType (Types.fromClass (Void.TYPE));
        }

        /* Either get the field directly, if it's public. */
        if (Modifier.isPublic (field.getModifiers ())) {
            System.out.println ("Field " + fieldName + " is public.");
            getterOrSetter = null;
        }else {
            /* Or find a getter/setter depending on the access mode. */
            if (mode == AccessMode.get) {
                String getterName = IdUtil.createGetterName (field);
                try {
                    Method localGetter = childType.getJavaType ().getMethod (getterName);
                    if (localGetter.getReturnType ().equals (field.getType ())) {
                        if (Modifier.isPublic (localGetter.getModifiers ())) {
                            getterOrSetter = localGetter;
                        } else {
                            throw new BrygJitException ("The supposed getter " + getterName + " is not public.", getLine ());
                        }
                    } else {
                        throw new BrygJitException ("The return type of the supposed getter " + getterName +
                                " does not match the field's type.", getLine ());
                    }
                } catch (NoSuchMethodException e) {
                    throw new BrygJitException ("Getter " + getterName + " of type " + field.getType () +
                            " not found but expected.", getLine ());
                }
            }else if (mode == AccessMode.set) {
                String setterName = IdUtil.createSetterName (field);
                try {
                    Method localSetter = childType.getJavaType ().getMethod (setterName, field.getType ());
                    if (Modifier.isPublic (localSetter.getModifiers ())) {
                        getterOrSetter = localSetter;
                    } else {
                        throw new BrygJitException ("The supposed setter " + setterName + " is not public.", getLine ());
                    }
                } catch (NoSuchMethodException e) {
                    throw new BrygJitException ("Setter " + setterName + " of type " + field.getType ()
                            + " not found but expected.", getLine ());
                }
            }
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor ();

        child.compile ();
        String childInternalName = child.getType ().getInternalName ();

        if (mode == AccessMode.get) {
            if (getterOrSetter != null) {
                mv.visitMethodInsn (INVOKEVIRTUAL, childInternalName,
                        getterOrSetter.getName (), TypeHelper.generateMethodDesc (
                                null,
                                type
                        ), false);
                // -> T
            }else {
                /* Get the field directly. */
                mv.visitFieldInsn (GETFIELD, childInternalName,
                        field.getName (), type.getDescriptor ());
                // -> T
            }
        }else { /* AccessMode.set */
            /* This has to be checked now, because the setFieldExpression can be set by its setter. */
            if (setFieldExpression == null) {
                throw new BrygJitException ("A setFieldExpression has to be supplied to the constructor for" +
                        " a correct setter order. If you see this exception as a user, this is likely a compiler bug!", getLine ());
            }
            setFieldExpression.compile ();

            Type fieldType = Types.fromClass (field.getType ());
            if (getterOrSetter != null) {
                mv.visitMethodInsn (INVOKEVIRTUAL, childInternalName,
                        getterOrSetter.getName (), TypeHelper.generateMethodDesc (
                                new Type[] { fieldType },
                                Types.fromClass (Void.TYPE)
                        ), false);
                // T ->
            }else {
                /* Set the field directly. */
                mv.visitFieldInsn (PUTFIELD, childInternalName,
                        field.getName (), fieldType.getDescriptor ());
                // T ->
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
