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
        System.out.println ("Scope[" + getClass () + "]: " + name + "    " + variable);
        if (variable == null && parent != null) {
            variable = parent.getVariable (name);
        }
        return variable;
    }

    /**
     * @return The same object, for chaining.
     */
    public LocalVariable registerLocalVariable (LocalVariable variable) {
        variable.setId (calculateNextId (variable.getType ()));
        registerVariableInternal (variable);
        return variable;
    }

    /**
     * @return The same object, for chaining.
     */
    public InstanceVariable registerInstanceVariable (InstanceVariable variable) {
        registerVariableInternal (variable);
        return variable;
    }

    /**
     * This method should NOT be used anywhere outside of the Scope class.
     * It is only protected for overriding methods, otherwise it would be private.
     * Use the registerLocalVariable and registerInstanceVariable methods instead.
     */
    protected void registerVariableInternal (Variable variable) {
        variables.put (variable.getName (), variable);
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
