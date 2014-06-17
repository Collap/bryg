package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.expression.*;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.util.TypeHelper;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.InvalidInputParameterException;
import org.objectweb.asm.Label;

import static org.objectweb.asm.Opcodes.*;

public class InDeclarationNode extends Node {

    private Variable parameter;

    public InDeclarationNode (RenderVisitor visitor, BrygParser.InDeclarationContext ctx) throws ClassNotFoundException {
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
                ((ClassType) model.getType ()).getJvmName (),
                "getVariable",
                TypeHelper.generateMethodDesc (
                        new Type[] { ClassType.STRING},
                        ClassType.OBJECT
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
        ClassType exceptionClass = InvalidInputParameterException.CLASS_TYPE;
        method.visitTypeInsn (NEW, exceptionClass.getJvmName ());
        method.visitInsn (DUP);
        // -> InvalidParameterException, InvalidParameterException

        method.visitLdcInsn (parameter.getName () + " could not be loaded!");
        // -> String

        method.visitMethodInsn (INVOKESPECIAL, exceptionClass.getJvmName (), "<init>",
                TypeHelper.generateMethodDesc (
                        new Type[] { ClassType.STRING},
                        PrimitiveType._void
                ),
                false);
        // InvalidParameterException, String ->

        method.visitInsn (ATHROW);
        // InvalidParameterException ->

        method.visitFrame (F_SAME1, 0, null, 1, new Object[] { ClassType.OBJECT.getJvmName () });
        method.visitLabel (skipException);
    }

    private void castAndStore () {
        Type type = parameter.getType ();
        if (type instanceof ClassType) {
            castAndStoreObject ();
        }else { /* PrimitiveType. */
            switch ((PrimitiveType) type) {
                // case _boolean:   castAndStoreBool (); break;
                // case _char:   castAndStoreChar (); break;
                // case _byte:   castAndStoreByte (); break;
                // case _short:  castAndStoreShort (); break;
                case _int:    castAndStoreInt (); break;
                // case _long:   castAndStoreLong (); break;
                // case _float:  castAndStoreFloat (); break;
                // case _double: castAndStoreDouble (); break;
            }
        }
    }

    private void castAndStoreObject () {
        BrygMethodVisitor method = visitor.getMethod ();
        ClassType type = (ClassType) parameter.getType ();

        method.visitTypeInsn (CHECKCAST, type.getJvmName ());
        // Object -> T

        method.visitVarInsn (ASTORE, parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { type.getJvmName () }, 0, null);
        // T ->
    }

    private void castAndStoreInt () {
        BrygMethodVisitor method = visitor.getMethod ();

        method.visitTypeInsn (CHECKCAST, ClassType.INTEGER.getJvmName ());
        // Object -> Integer

        method.visitMethodInsn (INVOKEVIRTUAL, ClassType.INTEGER.getJvmName (), "intValue",
                TypeHelper.generateMethodDesc (
                        null,
                        PrimitiveType._int
                ),
                false
        );
        // Integer -> int

        method.visitVarInsn (ISTORE, parameter.getId ());
        method.visitFrame (F_APPEND, 1, new Object[] { INTEGER }, 0, null);
    }

}
