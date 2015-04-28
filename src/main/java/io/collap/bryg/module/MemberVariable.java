package io.collap.bryg.module;

import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.VariableUsageInfo;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.CompiledVariable;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.CHECKCAST;
import static bryg.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static bryg.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MemberVariable<T> extends CompiledVariable implements Member<VariableUsageInfo> {

    private Module owner;
    private @Nullable T value;

    /**
     * @throws java.lang.IllegalArgumentException When the value is null despite Nullness being notnull.
     */
    public MemberVariable(Module owner, Type type, String name, @Nullable T value, Nullness nullness) {
        super(type, name, Mutability.immutable, nullness);

        if (nullness == Nullness.notnull && value == null) {
            throw new IllegalArgumentException("The supplied value is null despite the variable being notnull.");
        }

        this.owner = owner;
        this.value = value;
    }

    /**
     * This method is both declared in the CompiledVariable class and the Member interface,
     * but the two declarations are contractually equivalent, which means that this override
     * is valid.
     */
    @Override
    public void compile(CompilationContext compilationContext, VariableUsageInfo usage) {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();
        if (usage.getAccessMode() == AccessMode.get) {
            if (value == null) {
                mv.visitInsn(Opcodes.ACONST_NULL);
            } else {
                if (type.isPrimitive() || type.isWrapperType() || type.similarTo(String.class)) {
                    mv.visitLdcInsn(value);
                } else { // Any Object, except a String (which is compiled as a constant).
                    // Load the module from the environment.
                    CompiledVariable environmentVariable = compilationContext.getUnitScope().getEnvironmentField();

                    environmentVariable.compile(compilationContext, VariableUsageInfo.withGetMode());
                    // -> StandardEnvironment

                    String moduleName = owner.getName();
                    mv.visitLdcInsn(moduleName);
                    // -> String

                    mv.visitMethodInsn(INVOKEVIRTUAL, environmentVariable.getType().getInternalName(),
                            "getModule", TypeHelper.generateMethodDesc(
                                    new Class[]{String.class},
                                    Module.class
                            ), false);
                    // StandardEnvironment, String -> Module


                    // Load this object via the module.
                    mv.visitLdcInsn(name);
                    // -> String

                    mv.visitMethodInsn(INVOKEINTERFACE, Types.fromClass(Module.class).getInternalName(),
                            "getMember", TypeHelper.generateMethodDesc(
                                    new Class[]{String.class},
                                    Member.class
                            ), true);
                    // Module, String -> Member

                    Type memberVariableType = Types.fromClass(MemberVariable.class);

                    // Cast the member to a MemberVariable and retrieve the value.
                    mv.visitTypeInsn(CHECKCAST, memberVariableType.getInternalName());
                    // Member -> MemberVariable

                    mv.visitMethodInsn(INVOKEVIRTUAL, memberVariableType.getInternalName(),
                            "getValue", TypeHelper.generateMethodDesc(
                                    null,
                                    Object.class // T erases to Object
                            ), false);
                    // MemberVariable -> Object

                    mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                    // Object -> T
                }
            }
        } else {
            throw new UnsupportedOperationException("A module member variable can only be retrieved, not set.");
        }
    }

    @Override
    public Type getResultType() {
        return type;
    }

    public @Nullable T getValue() {
        return value;
    }

    /**
     * This method is explicitly overridden, because it is both
     * declared by CompiledVariable and the Member interface.
     */
    @Override
    public String getName() {
        return super.getName();
    }

}
