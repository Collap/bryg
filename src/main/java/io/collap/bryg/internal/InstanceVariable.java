package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;
import static bryg.org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * This is explicitly an instance variable (field) of the <b>current template or closure</b>.
 */
public class InstanceVariable extends CompiledVariable {

    public InstanceVariable(FieldInfo field) {
        this(field.getType(), field.getName(), field.getMutability(), field.getNullness());
    }

    public InstanceVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    @Override
    public void compile(CompilationContext compilationContext, VariableUsageInfo usage) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        compilationContext.getFunctionScope().getThisVariable()
                .compile(compilationContext, VariableUsageInfo.withGetMode());
        // -> this

        int fieldOperation;
        if (usage.getAccessMode() == AccessMode.get) {
            fieldOperation = GETFIELD;
            // When compiled: this -> T
        } else {
            usage.getRightExpression().compile();
            // -> T

            fieldOperation = PUTFIELD;
            // When compiled: this, T ->
        }

        mv.visitFieldInsn(fieldOperation, compilationContext.getUnitType().getInternalName(),
                name, type.getDescriptor());
    }

}
