package io.collap.bryg.internal.scope;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;

import static bryg.org.objectweb.asm.Opcodes.ILOAD;
import static bryg.org.objectweb.asm.Opcodes.ISTORE;

public class LocalVariable extends CompiledVariable {

    public static final int INVALID_ID = -1;

    /**
     * The ID is guaranteed to be set once the nodes are compiled, but not before.
     */
    private int id = INVALID_ID;

    public LocalVariable(Type type, String name, Mutability mutability, Nullness nullness, int id) {
        super(type, name, mutability, nullness);
        this.id = id;
    }

    @Override
    public void compile(Context context, VariableExpression expression) {
        BrygMethodVisitor mv = context.getMethodVisitor();
        if (expression.getMode() == AccessMode.get) {
            mv.visitVarInsn(type.getOpcode(ILOAD), getId());
            // -> T
        } else { /* AccessMode.set */
            expression.getRightExpression().compile();
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
