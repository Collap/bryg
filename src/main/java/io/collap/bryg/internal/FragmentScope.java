package io.collap.bryg.internal;

import java.util.List;

public class FragmentScope extends FunctionScope {

    public FragmentScope(UnitScope parent, UnitType unitType, List<ParameterInfo> parameters) {
        super(parent, unitType, parameters);
    }

    public CompiledVariable getWriterVariable() {
        return getMandatoryVariable("writer");
    }

}
