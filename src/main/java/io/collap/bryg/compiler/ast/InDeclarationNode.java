package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.util.IdUtil;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.parser.BrygParser;
import bryg.org.objectweb.asm.Label;

import static bryg.org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;
    private Variable model;

    public InDeclarationNode (Context context, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        this (
                context,
                IdUtil.idToString (ctx.id ()),
                new TypeInterpreter (context.getClassResolver ()).interpretType (ctx.type ()),
                ctx.getStart ().getLine ()
        );
    }

    public InDeclarationNode (Context context, String name, Type type, int line) {
        this (context, context.getCurrentScope ().registerVariable (name, type), line);
    }

    public InDeclarationNode (Context context, Variable parameter, int line) {
        super (context);
        setLine (line);

        this.parameter = parameter;
        model = context.getCurrentScope ().getVariable ("model");
    }

    @Override
    public void compile () {
        /* Get, check, cast and store variable. */
        loadVariable ();
        ifNullThrowException ();
        castAndStore ();
    }

    private void loadVariable () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitVarInsn (ALOAD, model.getId ());
        // -> Model

        mv.visitLdcInsn (parameter.getName ());
        // -> String

        mv.visitMethodInsn (INVOKEINTERFACE,
                model.getType ().getAsmType ().getInternalName (),
                "getVariable",
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { String.class },
                        Object.class
                ),
                true);
        // Model, String -> Object
    }

    private void ifNullThrowException () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        mv.visitInsn (DUP);
        // Object -> Object, Object

        Label skipException = new Label ();
        mv.visitJumpInsn (IFNONNULL, skipException); /* Jump only when the reference is not null. */
        // Object ->

        /* Throw exception when the null check failed. */
        String exceptionInternalName = AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName ();
        mv.visitTypeInsn (NEW, exceptionInternalName);
        mv.visitInsn (DUP);
        // -> InvalidParameterException, InvalidParameterException

        mv.visitLdcInsn (parameter.getName () + " could not be loaded!");
        // -> String

        mv.visitMethodInsn (INVOKESPECIAL, exceptionInternalName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { String.class },
                        Void.TYPE
                ),
                false);
        // InvalidParameterException, String ->

        mv.visitInsn (ATHROW);
        // InvalidParameterException ->

        mv.visitLabel (skipException);
    }

    private void castAndStore () {
        Type type = parameter.getType ();
        if (type.getJavaType ().isPrimitive ()) {
            if (type.equals (Boolean.TYPE)) {
                castAndStorePrimitive (Boolean.class, Boolean.TYPE, "booleanValue");
            }else if (type.equals (Character.TYPE)) {
                castAndStorePrimitive (Character.class, Character.TYPE, "charValue");
            }else if (type.equals (Byte.TYPE)) {
                castAndStorePrimitive (Byte.class, Byte.TYPE, "byteValue");
            }else if (type.equals (Short.TYPE)) {
                castAndStorePrimitive (Short.class, Short.TYPE, "shortValue");
            }else if (type.equals (Integer.TYPE)) {
                castAndStorePrimitive (Integer.class, Integer.TYPE, "intValue");
            }else if (type.equals (Long.TYPE)) {
                castAndStorePrimitive (Long.class, Long.TYPE, "longValue");
            }else if (type.equals (Float.TYPE)) {
                castAndStorePrimitive (Float.class, Float.TYPE, "floatValue");
            }else if (type.equals (Double.TYPE)) {
                castAndStorePrimitive (Double.class, Double.TYPE, "doubleValue");
            }
        }else {
            castAndStoreObject ();
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        String internalTypeName = parameter.getType ().getAsmType ().getInternalName ();

        mv.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        mv.visitVarInsn (ASTORE, parameter.getId ());
        // T ->
    }

    private void castAndStorePrimitive (Class<?> objectClass, Class<?> primitiveClass, String valueMethodName) {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        String internalTypeName = AsmTypes.getAsmType (objectClass).getInternalName ();

        mv.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        mv.visitMethodInsn (INVOKEVIRTUAL, internalTypeName, valueMethodName,
                TypeHelper.generateMethodDesc (
                        null,
                        primitiveClass
                ),
                false
        );
        // Integer -> int

        mv.visitVarInsn (AsmTypes.getAsmType (primitiveClass).getOpcode (ISTORE), parameter.getId ());
    }

}
