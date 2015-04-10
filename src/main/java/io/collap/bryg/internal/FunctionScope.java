package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.compiler.VariableIdCounter;

import javax.annotation.Nullable;
import java.util.List;

public abstract class FunctionScope extends LocalScope {

    public FunctionScope(UnitScope parent, UnitType unitType, @Nullable List<ParameterInfo> parameters) {
        // Pass a new VariableIdCounter, since the ids are tied to the fragment.
        super(parent, new VariableIdCounter());

        registerLocalVariable(new LocalVariable(unitType, "this", Mutability.immutable, Nullness.notnull));

        if (parameters != null) {
            for (ParameterInfo parameter : parameters) {
                registerLocalVariable(new LocalVariable(parameter));
            }
        }
    }

    public CompiledVariable getThisVariable() {
        return getMandatoryVariable("this");
    }

}
