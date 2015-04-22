package io.collap.bryg.internal;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructorScope extends FunctionScope {

    public ConstructorScope(UnitScope parent, UnitType unitType, @Nullable List<ParameterInfo> parameters) {
        super(parent, unitType, parameters);
    }

    public CompiledVariable getEnvironmentVariable() {
        return getMandatoryVariable(StandardUnit.ENVIRONMENT_FIELD_NAME);
    }

}
