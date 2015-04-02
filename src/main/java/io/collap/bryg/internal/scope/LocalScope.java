package io.collap.bryg.internal.scope;

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

}
