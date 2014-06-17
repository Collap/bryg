package io.collap.bryg.compiler.parser;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BrygClassVisitor extends ClassVisitor {

    public BrygClassVisitor (ClassVisitor cv) {
        super (Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod (int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor method = super.visitMethod (access, name, desc, signature, exceptions);
        return new BrygMethodVisitor (method);
    }

}
