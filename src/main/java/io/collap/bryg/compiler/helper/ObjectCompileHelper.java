package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.type.Type;

import javax.annotation.Nullable;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public abstract class ObjectCompileHelper extends CompileHelper {

    protected Type type;

    public ObjectCompileHelper (BrygMethodVisitor method, Type type) {
        super (method);
        this.type = type;
    }

    /**
     * -> Object
     */
    public void compileNew () {
        compileNew ("()V", null);
    }

    /**
     * -> Object
     */
    public void compileNew (String constructorDesc, @Nullable List<Node> arguments) {
        String internalTypeName = type.getAsmType ().getInternalName ();

        method.visitTypeInsn (NEW, internalTypeName);
        // -> Object

        method.visitInsn (DUP);
        // Object -> Object, Object

        if (arguments != null) {
            for (Node argument : arguments) {
                argument.compile ();
            }
        }

        method.visitMethodInsn (INVOKESPECIAL, internalTypeName, "<init>", constructorDesc, false);
        // Object ->
    }

    public void compileInvokeVirtual (String methodName, String methodDesc) {
        // TODO: Check if the method exists.

        method.visitMethodInsn (INVOKEVIRTUAL, type.getAsmType ().getInternalName (), methodName, methodDesc, false);
    }

}
