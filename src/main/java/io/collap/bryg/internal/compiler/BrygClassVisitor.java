package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.ClassVisitor;
import bryg.org.objectweb.asm.MethodVisitor;
import bryg.org.objectweb.asm.Opcodes;

public class BrygClassVisitor extends ClassVisitor {

    public BrygClassVisitor (ClassVisitor cv) {
        super (Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod (int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor method = super.visitMethod (access, name, desc, signature, exceptions);
        return new BrygMethodVisitor(method);
    }

}
