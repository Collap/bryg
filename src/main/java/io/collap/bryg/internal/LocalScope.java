package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.compiler.VariableIdCounter;

public class LocalScope extends Scope {

    private VariableIdCounter variableIdCounter;

    public LocalScope(Scope parent, VariableIdCounter variableIdCounter) {
        super(parent);
        this.variableIdCounter = variableIdCounter;
    }

    public void registerLocalVariable(LocalVariable variable) {
        variable.setId(variableIdCounter.next(variable.getType()));
        putVariable(variable);
    }

    public LocalScope createSubScope() {
        return new LocalScope(this, variableIdCounter);
    }

    public CompiledVariable createTemporalVariable(Type type, Mutability mutability, Nullness nullness) {
        int id = variableIdCounter.next(type);
        LocalVariable variable = new LocalVariable(type, "tmp_" + id, mutability, nullness, id);
        putVariable(variable);
        return variable;
    }

    public VariableIdCounter getVariableIdCounter() {
        return variableIdCounter;
    }

}
