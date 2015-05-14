package io.collap.bryg.internal;

import io.collap.bryg.Mutability;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This scope tracks another scope's variables that are used at least once by this scope or by this scope's children.
 * All of these variables are declared in this scope.
 *
 * All variables captured are declared as fields and adding them to the unit type (closure type) is taken
 * care of by this class.
 */
public class ClosureScope extends UnitScope {

    private Scope target;
    private ClosureType closureType;
    private List<CompiledVariable> capturedVariables = new ArrayList<>();

    public ClosureScope(Scope target, ClosureType closureType) {
        super(null);
        this.target = target;
        this.closureType = closureType;

        for (FieldInfo field : closureType.getFields()) {
            registerInstanceVariable(new InstanceVariable(field));
        }
    }

    @Override
    public @Nullable CompiledVariable getVariable(String name) {
        @Nullable CompiledVariable variable = super.getVariable(name); // Also covers already captured variables.
        if (variable == null) {
            @Nullable CompiledVariable targetVariable = target.getVariable(name);
            if (targetVariable != null) {
                // Track variable.
                capturedVariables.add(targetVariable);

                // Declare variable in this scope. All captured variables are declared immutable, because changing
                // them would not affect the captured variables outside the closure, which would be misleading to
                // the programmer.
                FieldInfo field = new FieldInfo(targetVariable.getType(), targetVariable.getName(),
                        Mutability.immutable, targetVariable.getNullness());
                closureType.addField(field);

                InstanceVariable instanceVariable = new InstanceVariable(field);
                registerInstanceVariable(instanceVariable);
                variable = instanceVariable;
            }
        }
        return variable;
    }

    /**
     * @return Returns the list of the <i>originally</i> captured variables. These specific variables do not exist in
     *         this scope. Instead, copies of them exist in this scope as fields.
     */
    public List<CompiledVariable> getCapturedVariables() {
        return capturedVariables;
    }

    public CompiledVariable getParentVariable() {
        return getMandatoryVariable(StandardClosure.PARENT_FIELD_NAME);
    }

}
