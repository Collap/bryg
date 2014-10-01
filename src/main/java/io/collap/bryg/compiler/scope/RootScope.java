package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.model.GlobalVariableModel;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class RootScope extends Scope {

    private GlobalVariableModel globalVariableModel;
    private Set<String> globalVariablesUsed = new HashSet<> ();

    private int nextId = 0;

    public RootScope (GlobalVariableModel globalVariableModel) {
        super (null);
        this.globalVariableModel = globalVariableModel;
    }

    @Override
    public Variable getVariable (String name) {
        Variable variable = variables.get (name);
        if (variable == null) {
            GlobalVariableModel.GlobalVariable globalVariable = globalVariableModel.getDeclaredVariable (name);
            if (globalVariable != null) {
                Type type = new Type (globalVariable.getType ());
                variable = new Variable (type, name, calculateNextId (type), false);
                variables.put (name, variable);
                globalVariablesUsed.add (name);
            }
        }

        return variable;
    }

    @Override
    public Variable registerVariable (String name, Type type, boolean isMutable) {
        /* Remove flag if global variable is overshadowed in the root scope. */
        if (globalVariablesUsed.contains (name)) {
            globalVariablesUsed.remove (name);
        }

        return super.registerVariable (name, type, isMutable);
    }

    @Override
    public int calculateNextId (@Nullable Type type) {
        int id = nextId;

        /* Double and long use two variable slots. */
        boolean isWide = false;
        if (type != null && type.getJavaType ().isPrimitive ()) {
            isWide = type.similarTo (Long.TYPE) || type.similarTo (Double.TYPE);
        }

        if (isWide) {
            nextId += 2;
        }else {
            nextId += 1;
        }

        return id;
    }

    public GlobalVariableModel getGlobalVariableModel () {
        return globalVariableModel;
    }

    public Set<String> getGlobalVariablesUsed () {
        return globalVariablesUsed;
    }

}
