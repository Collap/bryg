package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.compiler.CompilationContext;

public abstract class CompiledVariable extends VariableInfo {

    public CompiledVariable(Type type, String name, Mutability mutability, Nullness nullness) {
        super(type, name, mutability, nullness);
    }

    /**
     * Invalid states are checked before this method is called, such as a variable with AccessMode.set
     * and Mutability.immutable.
     */
    public abstract void compile(CompilationContext compilationContext, VariableUsageInfo information);

}
