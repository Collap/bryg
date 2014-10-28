package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Scope {

    protected Scope parent;
    protected Map<String, Variable> variables = new HashMap<> ();

    public Scope (Scope parent) {
        this.parent = parent;
    }

    public @Nullable Variable getVariable (String name) {
        Variable variable = variables.get (name);
        if (variable == null) {
            variable = parent.getVariable (name);
        }
        return variable;
    }

    /**
     * @return The same object, for chaining.
     */
    public Variable registerVariable (Variable variable) {
        variable.setId (calculateNextId (variable.getType ()));
        variables.put (variable.getName (), variable);
        return variable;
    }

    public int calculateNextId (@Nullable Type type) {
        return parent.calculateNextId (type);
    }

    public Scope createSubScope () {
        return new Scope (this);
    }

    public Scope getParent () {
        return parent;
    }

}
