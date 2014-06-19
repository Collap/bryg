package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.expression.*;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.TypeInterpreter;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.exception.InvalidInputParameterException;
import org.objectweb.asm.Label;

import static org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;

    public InDeclarationNode (StandardVisitor visitor, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
        super (visitor);

        String name = ctx.Id ().getText ();

        TypeInterpreter interpreter = new TypeInterpreter (visitor);
        parameter = visitor.getScope ().registerVariable (name, interpreter.interpretType (ctx.type ()));
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
        Variable model = visitor.getScope ().getVariable ("model");

        method.visitVarInsn (ALOAD, model.getId ());
        // -> Model

        method.visitLdcInsn (parameter.getName ());
        // -> String

        method.visitMethodInsn (INVOKEINTERFACE,
                Types.getAsmType (model.getType ()).getInternalName (),
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
        String exceptionInternalName = Types.getAsmType (InvalidInputParameterException.class).getInternalName ();
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

        method.visitFrame (F_SAME1, 0, null, 1, new Object[] { Types.getAsmType (Object.class).getInternalName () });
        method.visitLabel (skipException);
    }

    private void castAndStore () {
        Class<?> type = parameter.getType ();
        if (type.isPrimitive ()) {
            if (type.equals (Integer.TYPE)) {
                castAndStoreInt ();
            }
            /* switch (type) {
                // case _boolean:   castAndStoreBool (); break;
                // case _char:      castAndStoreChar (); break;
                // case _byte:      castAndStoreByte (); break;
                // case _short:     castAndStoreShort (); break;
                // case _int:       castAndStoreInt (); break;
                // case _long:      castAndStoreLong (); break;
                // case _float:     castAndStoreFloat (); break;
                // case _double:    castAndStoreDouble (); break;
            } */
        }else {
            castAndStoreObject ();
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor method = visitor.getMethod ();
        String internalTypeName = Types.getAsmType (parameter.getType ()).getInternalName ();

        method.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> T

        method.visitVarInsn (ASTORE, parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { internalTypeName }, 0, null);
        // T ->
    }

    private void castAndStoreInt () {
        BrygMethodVisitor method = visitor.getMethod ();
        String internalTypeName = Types.getAsmType (Integer.class).getInternalName ();

        method.visitTypeInsn (CHECKCAST, internalTypeName);
        // Object -> Integer

        method.visitMethodInsn (INVOKEVIRTUAL, internalTypeName, "intValue",
                TypeHelper.generateMethodDesc (
                        null,
                        Integer.TYPE
                ),
                false
        );
        // Integer -> int

        method.visitVarInsn (ISTORE, parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { INTEGER }, 0, null);
    }

}
