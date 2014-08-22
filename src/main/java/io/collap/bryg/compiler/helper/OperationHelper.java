package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;

import static org.objectweb.asm.Opcodes.*;

public class OperationHelper {

    public static void compileDup (BrygMethodVisitor mv, Type type) {
        if (type.is64Bit ()) {
            mv.visitInsn (DUP2);
        }else {
            mv.visitInsn (DUP);
        }
    }

}
