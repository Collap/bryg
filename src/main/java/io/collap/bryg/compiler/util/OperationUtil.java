package io.collap.bryg.compiler.util;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;

import static bryg.org.objectweb.asm.Opcodes.DUP;
import static bryg.org.objectweb.asm.Opcodes.DUP2;

public class OperationUtil {

    public static void compileDup (BrygMethodVisitor mv, Type type) {
        if (type.is64Bit ()) {
            mv.visitInsn (DUP2);
        }else {
            mv.visitInsn (DUP);
        }
    }

}
