package io.collap.bryg.compiler.parser;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * This class assumes the following parameters to the method:
 *      1   Writer
 *      2   Model
 * TODO: Adapt to current architecture.
 */
public class BrygMethodVisitor extends MethodVisitor {

    private StringBuilder builder;

    public BrygMethodVisitor (MethodVisitor visitor) {
        super (Opcodes.ASM5, visitor);
        builder = new StringBuilder ();
    }



    public void writeConstantString (String string) {
        builder.append (string);
    }

    /**
     * This method HAS to access the child method visitor to not trigger a string builder flush on every write.
     *
     * Stack: -
     */
    private void flushStringBuilder () {
        if (builder.length () > 0) {
            String string = builder.toString ();
            builder.setLength (0); /* Empty builder. */
            mv.visitVarInsn (ALOAD, 1);     // Writer
            mv.visitLdcInsn (string);       // string
            mv.visitMethodInsn (INVOKEVIRTUAL, "java/io/Writer", "write", "(Ljava/lang/String;)V", false);
        }
    }

    /**
     * Stack: -> Writer
     */
    public void loadWriter () {
        visitVarInsn (ALOAD, 1);
    }

    /**
     * Stack: -
     */
    public void visitLabelInSameFrame (Label label) {
        visitLabel (label);
        visitFrame (F_SAME, 0, null, 0, null);
    }

    /**
     * Stack: String, Writer ->
     */
    public void writeString () {
        visitMethodInsn (INVOKEVIRTUAL, "java/io/Writer", "write", "(Ljava/lang/String;)V", false);
    }

    /**
     * Stack: -
     */
    public void voidReturn () {
        visitInsn (RETURN);
    }

    @Override
    public void visitParameter (String name, int access) {
        flushStringBuilder ();
        super.visitParameter (name, access);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault () {
        flushStringBuilder ();
        return super.visitAnnotationDefault ();
    }

    @Override
    public AnnotationVisitor visitAnnotation (String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitAnnotation (desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation (int typeRef, TypePath typePath, String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitTypeAnnotation (typeRef, typePath, desc, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation (int parameter, String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitParameterAnnotation (parameter, desc, visible);
    }

    @Override
    public void visitAttribute (Attribute attr) {
        flushStringBuilder ();
        super.visitAttribute (attr);
    }

    @Override
    public void visitCode () {
        flushStringBuilder ();
        super.visitCode ();
    }

    @Override
    public void visitFrame (int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        flushStringBuilder ();
        super.visitFrame (type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitInsn (int opcode) {
        flushStringBuilder ();
        super.visitInsn (opcode);
    }

    @Override
    public void visitIntInsn (int opcode, int operand) {
        flushStringBuilder ();
        super.visitIntInsn (opcode, operand);
    }

    @Override
    public void visitVarInsn (int opcode, int var) {
        flushStringBuilder ();
        super.visitVarInsn (opcode, var);
    }

    @Override
    public void visitTypeInsn (int opcode, String type) {
        flushStringBuilder ();
        super.visitTypeInsn (opcode, type);
    }

    @Override
    public void visitFieldInsn (int opcode, String owner, String name, String desc) {
        flushStringBuilder ();
        super.visitFieldInsn (opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn (int opcode, String owner, String name, String desc, boolean itf) {
        flushStringBuilder ();
        super.visitMethodInsn (opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn (String name, String desc, Handle bsm, Object... bsmArgs) {
        flushStringBuilder ();
        super.visitInvokeDynamicInsn (name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn (int opcode, Label label) {
        flushStringBuilder ();
        super.visitJumpInsn (opcode, label);
    }

    @Override
    public void visitLabel (Label label) {
        flushStringBuilder ();
        super.visitLabel (label);
    }

    @Override
    public void visitLdcInsn (Object cst) {
        flushStringBuilder ();
        super.visitLdcInsn (cst);
    }

    @Override
    public void visitIincInsn (int var, int increment) {
        flushStringBuilder ();
        super.visitIincInsn (var, increment);
    }

    @Override
    public void visitTableSwitchInsn (int min, int max, Label dflt, Label... labels) {
        flushStringBuilder ();
        super.visitTableSwitchInsn (min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn (Label dflt, int[] keys, Label[] labels) {
        flushStringBuilder ();
        super.visitLookupSwitchInsn (dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn (String desc, int dims) {
        flushStringBuilder ();
        super.visitMultiANewArrayInsn (desc, dims);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation (int typeRef, TypePath typePath, String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitInsnAnnotation (typeRef, typePath, desc, visible);
    }

    @Override
    public void visitTryCatchBlock (Label start, Label end, Label handler, String type) {
        flushStringBuilder ();
        super.visitTryCatchBlock (start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation (int typeRef, TypePath typePath, String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitTryCatchAnnotation (typeRef, typePath, desc, visible);
    }

    @Override
    public void visitLocalVariable (String name, String desc, String signature, Label start, Label end, int index) {
        flushStringBuilder ();
        super.visitLocalVariable (name, desc, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation (int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        flushStringBuilder ();
        return super.visitLocalVariableAnnotation (typeRef, typePath, start, end, index, desc, visible);
    }

    @Override
    public void visitLineNumber (int line, Label start) {
        flushStringBuilder ();
        super.visitLineNumber (line, start);
    }

    @Override
    public void visitMaxs (int maxStack, int maxLocals) {
        flushStringBuilder ();
        super.visitMaxs (maxStack, maxLocals);
    }

    @Override
    public void visitEnd () {
        flushStringBuilder ();
        super.visitEnd ();
    }

}
