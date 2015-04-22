package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;

import static bryg.org.objectweb.asm.Opcodes.ILOAD;
import static bryg.org.objectweb.asm.Opcodes.ISTORE;

public class LocalVariable extends CompiledVariable {

    public static final int INVALID_ID = -1;

    /**
     * The ID is guaranteed to be set once the nodes are compiled, but not before.
     */
    private int id = INVALID_ID;

    public LocalVariable(ParameterInfo parameter) {
        this(parameter.getType(), parameter.getName(), parameter.getMutability(), parameter.getNullness());
    }

    public LocalVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    public LocalVariable(Type type, String name, Mutability mutability, Nullness nullness, int id) {
        this(type, name, mutability, nullness);
        this.id = id;
    }

    @Override
    public void compile(CompilationContext compilationContext, VariableUsageInfo usageInfo) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();
        if (usageInfo.getAccessMode() == AccessMode.get) {
            mv.visitVarInsn(type.getOpcode(ILOAD), getId());
            // -> T
        } else { /* AccessMode.set */
            usageInfo.getRightExpression().compile();
            mv.visitVarInsn(type.getOpcode(ISTORE), getId());
            // T ->
        }
    }

    public int getId() {
        if (id == INVALID_ID) {
            throw new BrygJitException("The ID of the variable '" + getName() + "' has not been set yet.", -1); // TODO: Line?
        }

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
