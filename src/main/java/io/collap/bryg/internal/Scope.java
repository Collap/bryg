package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class Scope {

    protected @Nullable Scope parent;
    protected Map<String, CompiledVariable> variables = new HashMap<>();

    public Scope(@Nullable Scope parent) {
        this.parent = parent;
    }

    public @Nullable CompiledVariable getVariable(String name) {
        @Nullable CompiledVariable variable = variables.get(name);
        if (variable == null && parent != null) {
            variable = parent.getVariable(name);
        }
        return variable;
    }

    /**
     * Can be called to retrieve a variable that is guaranteed to be registered.
     * @throws java.lang.IllegalStateException If the requested variable is not registered.
     */
    public CompiledVariable getMandatoryVariable(String name) {
        @Nullable CompiledVariable variable = variables.get(name);
        if (variable == null) {
            throw new IllegalStateException("The (mandatory) variable '" + name + "' is not defined. " +
                    "This is most likely a compiler bug.");
        }
        return variable;
    }

    protected void putVariable(CompiledVariable variable) {
        variables.put(variable.getName(), variable);
    }

    public @Nullable Scope getParent() {
        return parent;
    }

}
