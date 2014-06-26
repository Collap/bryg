package io.collap.bryg.compiler.expression;

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

    public Variable getVariable (String name) {
        Variable variable = variables.get (name);
        if (variable == null) {
            variable = parent.getVariable (name);
        }
        return variable;
    }

    public Variable registerVariable (String name, Type type) {
        Variable variable = new Variable (type, name, calculateNextId (type));
        variables.put (name, variable);
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
