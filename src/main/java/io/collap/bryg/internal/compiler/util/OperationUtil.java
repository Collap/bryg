package io.collap.bryg.internal.compiler.util;

import bryg.org.objectweb.asm.Label;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.Type;

import static bryg.org.objectweb.asm.Opcodes.*;

public class OperationUtil {

    public static void compileDup (BrygMethodVisitor mv, Type type) {
        if (type.is64Bit ()) {
            mv.visitInsn (DUP2);
        }else {
            mv.visitInsn (DUP);
        }
    }

    /**
     * Accepts a reference on the stack and throws an exception should the reference be null.
     */
    public static void compileIfNullThrowException (BrygMethodVisitor mv, Type exceptionType, String message) {
        mv.visitInsn (DUP);
        // Object -> Object, Object

        Label skipException = new Label ();
        mv.visitJumpInsn (IFNONNULL, skipException); /* Jump only when the reference is not null. */
        // Object ->

        /* Throws the exception. */
        new ExceptionCompileHelper (mv, exceptionType).compileThrow (message);

        mv.visitLabel (skipException);
    }

}
