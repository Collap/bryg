package io.collap.bryg.internal;

import java.util.List;

public class ConstructorInfo extends FunctionInfo {

    // TODO: Add the environment field as a standard parameter.

    public ConstructorInfo(UnitType owner, String name, List<ParameterInfo> parameters) {
        super(owner, name, parameters);
    }

}
