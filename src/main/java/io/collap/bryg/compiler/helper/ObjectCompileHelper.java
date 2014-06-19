package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.type.Types;

import javax.annotation.Nullable;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public abstract class ObjectCompileHelper extends CompileHelper {

    protected Class<?> type;

    public ObjectCompileHelper (StandardVisitor visitor, Class<?> type) {
        super (visitor);
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
        BrygMethodVisitor method = visitor.getMethod ();
        String internalTypeName = Types.getAsmType (type).getInternalName ();

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
        BrygMethodVisitor method = visitor.getMethod ();

        // TODO: Check if the method exists.

        method.visitMethodInsn (INVOKEVIRTUAL, Types.getAsmType (type).getInternalName (), methodName, methodDesc, false);
    }

}
