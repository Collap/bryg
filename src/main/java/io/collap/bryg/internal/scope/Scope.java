package io.collap.bryg.internal.scope;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class Scope {

    protected Scope parent;
    protected Map<String, CompiledVariable> variables = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public @Nullable CompiledVariable getVariable(String name) {
        CompiledVariable variable = variables.get(name);
        System.out.println("Scope[" + getClass() + "]: " + name + "    " + variable);
        if (variable == null && parent != null) {
            variable = parent.getVariable(name);
        }
        return variable;
    }

    protected void putVariable(CompiledVariable variable) {
        variables.put(variable.getName(), variable);
    }

    public Scope getParent() {
        return parent;
    }

}
