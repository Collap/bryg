package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.type.TypeHelper;

import static bryg.org.objectweb.asm.Opcodes.*;
import static bryg.org.objectweb.asm.Opcodes.ATHROW;

public class ExceptionCompileHelper extends ObjectCompileHelper {

    public ExceptionCompileHelper (BrygMethodVisitor mv, Type type) {
        super (mv, type);
    }

    public void compileThrow (String message) {
        String exceptionInternalName = type.getInternalName ();
        mv.visitTypeInsn (NEW, exceptionInternalName);
        mv.visitInsn (DUP);
        // -> InvalidParameterException, InvalidParameterException

        mv.visitLdcInsn (message);
        // -> String

        mv.visitMethodInsn (INVOKESPECIAL, exceptionInternalName, "<init>",
                TypeHelper.generateMethodDesc (
                        new Class<?>[]{String.class},
                        Void.TYPE
                ),
                false);
        // InvalidParameterException, String ->

        mv.visitInsn (ATHROW);
        // InvalidParameterException ->
    }

}
