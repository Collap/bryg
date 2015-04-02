package io.collap.bryg.internal.scope;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;
import static bryg.org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * This is explicitly an instance variable (field) of the <b>current template or closure</b>.
 */
public class InstanceVariable extends CompiledVariable {

    public InstanceVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    @Override
    public void compile(Context context, VariableExpression expression) {
        BrygMethodVisitor mv = context.getMethodVisitor();

        CompiledVariable thisVar = context.getHighestLocalScope().getVariable("this");
        new VariableExpression(context, expression.getLine(), thisVar, AccessMode.get).compile();
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

        mv.visitFieldInsn(fieldOperation, context.getUnitType().getInternalName(), name, type.getDescriptor());
    }

}
