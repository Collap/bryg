package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
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
    public void compile(CompilationContext compilationContext, VariableExpression expression) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();

        new VariableExpression(compilationContext, expression.getLine(),
                compilationContext.getFunctionScope().getThisVariable(), AccessMode.get).compile();
        // -> this

        int fieldOperation;
        if (expression.getMode() == AccessMode.get) {
            fieldOperation = GETFIELD;
            // When compiled: this -> T
        } else {
            expression.getRightExpression().compile();
            // -> T

            fieldOperation = PUTFIELD;
            // When compiled: this, T ->
        }

        mv.visitFieldInsn(fieldOperation, compilationContext.getUnitType().getInternalName(), name, type.getDescriptor());
    }

}
