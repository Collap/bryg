package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;

import static bryg.org.objectweb.asm.Opcodes.*;
import static bryg.org.objectweb.asm.Opcodes.ATHROW;

public class ExceptionCompileHelper extends ObjectCompileHelper {

    public ExceptionCompileHelper (BrygMethodVisitor mv, Type type) {
        super (mv, type);
    }

    public void compileThrow (String message) {
        String exceptionInternalName = type.getAsmType ().getInternalName ();
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
