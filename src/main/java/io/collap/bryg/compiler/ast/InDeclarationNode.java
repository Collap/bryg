package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.expression.*;
import io.collap.bryg.compiler.helper.IdHelper;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.exception.InvalidInputParameterException;
import org.objectweb.asm.Label;

import static org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;
    private Variable model;

    public InDeclarationNode (StandardVisitor visitor, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        super (visitor);
        setLine (ctx.getStart ().getLine ());

        String name = IdHelper.idToString (ctx.id ());

        TypeInterpreter interpreter = new TypeInterpreter (visitor);
        parameter = visitor.getCurrentScope ().registerVariable (name, interpreter.interpretType (ctx.type ()));
        model = visitor.getCurrentScope ().getVariable ("model");
    }

    @Override
    public void compile () {
        /* Get, check, cast and store variable. */
        loadVariable ();
        ifNullThrowException ();
        castAndStore ();
    }

    private void loadVariable () {
        BrygMethodVisitor method = visitor.getMethod ();

        method.visitVarInsn (ALOAD, model.getId ());
        // -> Model

        method.visitLdcInsn (parameter.getName ());
        // -> String

        method.visitMethodInsn (INVOKEINTERFACE,
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
        BrygMethodVisitor method = visitor.getMethod ();

        method.visitInsn (DUP);
        // Object -> Object, Object

        Label skipException = new Label ();
        method.visitJumpInsn (IFNONNULL, skipException); /* Jump only when the reference is not null. */
        // Object ->

        /* Throw exception when the null check failed. */
        String exceptionInternalName = AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName ();
        method.visitTypeInsn (NEW, exceptionInternalName);
        method.visitInsn (DUP);
        // -> InvalidParameterException, InvalidParameterException

        method.visitLdcInsn (parameter.getName () + " could not be loaded!");
        // -> String

        method.visitMethodInsn (INVOKESPECIAL, exceptionInternalName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { String.class },
                        Void.TYPE
                ),
                false);
        // InvalidParameterException, String ->

        method.visitInsn (ATHROW);
        // InvalidParameterException ->

        method.visitFrame (F_SAME1, 0, null, 1, new Object[] { AsmTypes.getAsmType (Object.class).getInternalName () });
        method.visitLabel (skipException);
    }

    private void castAndStore () {
        Type type = parameter.getType ();
        if (type.getJavaType ().isPrimitive ()) {
            if (type.equals (Boolean.TYPE)) {
                castAndStorePrimitive (Boolean.class, Boolean.TYPE, "booleanValue", INTEGER);
            }else if (type.equals (Character.TYPE)) {
                castAndStorePrimitive (Character.class, Character.TYPE, "charValue", INTEGER);
            }else if (type.equals (Byte.TYPE)) {
                castAndStorePrimitive (Byte.class, Byte.TYPE, "byteValue", INTEGER);
            }else if (type.equals (Short.TYPE)) {
                castAndStorePrimitive (Short.class, Short.TYPE, "shortValue", INTEGER);
            }else if (type.equals (Integer.TYPE)) {
                castAndStorePrimitive (Integer.class, Integer.TYPE, "intValue", INTEGER);
            }else if (type.equals (Long.TYPE)) {
                castAndStorePrimitive (Long.class, Long.TYPE, "longValue", LONG);
            }else if (type.equals (Float.TYPE)) {
                castAndStorePrimitive (Float.class, Float.TYPE, "floatValue", FLOAT);
            }else if (type.equals (Double.TYPE)) {
                castAndStorePrimitive (Double.class, Double.TYPE, "doubleValue", DOUBLE);
            }
        }else {
            castAndStoreObject ();
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor method = visitor.getMethod ();
        String internalTypeName = parameter.getType ().getAsmType ().getInternalName ();

        method.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        method.visitVarInsn (ASTORE, parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { internalTypeName }, 0, null);
        // T ->
    }

    private void castAndStorePrimitive (Class<?> objectClass, Class<?> primitiveClass, String valueMethodName, Integer frameType) {
        BrygMethodVisitor method = visitor.getMethod ();
        String internalTypeName = AsmTypes.getAsmType (objectClass).getInternalName ();

        method.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        method.visitMethodInsn (INVOKEVIRTUAL, internalTypeName, valueMethodName,
                TypeHelper.generateMethodDesc (
                        null,
                        primitiveClass
                ),
                false
        );
        // Integer -> int

        method.visitVarInsn (AsmTypes.getAsmType (primitiveClass).getOpcode (ISTORE), parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { frameType }, 0, null);
    }

}
