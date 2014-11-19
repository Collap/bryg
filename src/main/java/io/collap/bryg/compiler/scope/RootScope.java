package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class RootScope extends Scope {

    private GlobalVariableModel globalVariableModel;
    private Set<String> globalVariablesUsed = new HashSet<> ();

    private int nextId = 0;

    public RootScope (GlobalVariableModel globalVariableModel) {
        super (null);
        this.globalVariableModel = globalVariableModel;

        /* Unless we change the way how types are handled by the compiler, we can not assign a proper type here. */
        registerVariable (new Variable (new Type (Object.class), "this", false));
        registerVariable (new Variable (new Type (Writer.class), "writer", false));
        registerVariable (new Variable (new Type (Model.class), "model", false));
    }

    @Override
    public @Nullable Variable getVariable (String name) {
        Variable variable = variables.get (name);
        if (variable == null) {
            GlobalVariableModel.GlobalVariable globalVariable = globalVariableModel.getDeclaredVariable (name);
            if (globalVariable != null) {
                Type type = new Type (globalVariable.getType ());
                variable = new Variable (type, name, false);
                variable.setId (calculateNextId (type));
                variables.put (name, variable);
                globalVariablesUsed.add (name);
            }
        }

        return variable;
    }

    @Override
    public Variable registerVariable (Variable variable) {
        /* Remove flag if global variable is overshadowed in the root scope. */
        if (globalVariablesUsed.contains (variable.getName ())) {
            globalVariablesUsed.remove (variable.getName ());
        }

        return super.registerVariable (variable);
    }

    @Override
    public int calculateNextId (@Nullable Type type) {
        int id = nextId;

        if (type != null) {
            nextId += type.getStackSize ();
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
