package io.collap.bryg.compiler.scope;

import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This scope tracks another scope's variables that are used at least once by this scope or by this scope's children.
 * All of these variables are declared in this scope.
 */
public class ClosureScope extends RootScope {

    private Scope target;
    private Set<Variable> usedVariables = new HashSet<> ();

    /**
     * Saves the originally captured variables.
     */
    private List<Variable> capturedVariables;

    public ClosureScope (Scope target) {
        super (new GlobalVariableModel ()); /* Provide an empty global variable model. */
        this.target = target;

        /* Register __parent variable. */
        registerVariable (new Variable (new Type (Object.class), ClosureType.PARENT_FIELD_NAME, false));
        registerVariable (new Variable (new Type (Model.class), ClosureType.PARENT_MODEL_FIELD_NAME, false));
    }

    @Override
    public @Nullable Variable getVariable (String name) {
        Variable variable = variables.get (name);
        if (variable == null) {
            Variable targetVariable = target.getVariable (name);

            if (targetVariable != null) {
                /* Track variable. */
                usedVariables.add (targetVariable);

                /* Declare variable in this scope.
                   All captured variables are declared immutable, because changing
                   them would not affect the captured variables outside the closure. */
                variable = new Variable (targetVariable.getType (), name, false, targetVariable.isNullable ());
                registerVariable (variable);
            }
        }
        return variable;
    }

    /**
     * @return Returns the list of captured variables in a well-defined order.
     */
    public List<Variable> getCapturedVariables () {
        if (capturedVariables == null) {
            capturedVariables = new ArrayList<> (usedVariables.size ());
            for (Variable variable : usedVariables) {
                capturedVariables.add (variable);
            }
        }

        return capturedVariables;
    }

}
