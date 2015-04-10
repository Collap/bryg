package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ConstructorScope extends FunctionScope {

    public ConstructorScope(UnitScope parent, UnitType unitType, @Nullable List<ParameterInfo> parameters) {
        super(parent, unitType, prependImplicitParameters(parameters));
    }

    private static List<ParameterInfo> prependImplicitParameters(@Nullable List<ParameterInfo> parameters) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        parameters.add(new ParameterInfo(Types.fromClass(StandardEnvironment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, Mutability.immutable, Nullness.notnull, null));
        return parameters;
    }

    public CompiledVariable getEnvironmentVariable() {
        return getMandatoryVariable(StandardUnit.ENVIRONMENT_FIELD_NAME);
    }

}
