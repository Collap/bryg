package io.collap.bryg.internal.scope;

import io.collap.bryg.internal.StandardClosure;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.GlobalVariableModel;
import io.collap.bryg.Model;
import io.collap.bryg.internal.UnitType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This scope tracks another scope's variables that are used at least once by this scope or by this scope's children.
 * All of these variables are declared in this scope.
 */
public class ClosureScope extends MethodScope {

    private Scope target;
    private Set<Variable> usedVariables = new HashSet<> ();

    /**
     * Saves the originally captured variables.
     */
    private List<Variable> capturedVariables;

    public ClosureScope (UnitScope unitScope, UnitType unitType, Scope target) {
        /* TODO: Provide an empty global variable model, because global variables are already captured by the parent fragment? */
        super (unitScope, unitType, new GlobalVariableModel (), null);
        this.target = target;

        registerLocalVariable (new LocalVariable (Types.fromClass (Model.class), "model", false, false));

        /* Register __parent variable. */
        registerInstanceVariable (new InstanceVariable (Types.fromClass (Object.class), StandardClosure.PARENT_FIELD_NAME, false));
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
                   them would not affect the captured variables outside the closure,
                   which would be misleading to the programmer. */
                InstanceVariable instanceVariable = new InstanceVariable (targetVariable.getType (), name,
                        false, targetVariable.isNullable ());
                registerInstanceVariable (instanceVariable);
                variable = instanceVariable;
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
