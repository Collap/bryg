package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.StandardUnit;
import io.collap.bryg.unit.UnitType;

public class ConstructorScope extends HighestLocalScope {

    public ConstructorScope (UnitScope parent, UnitType unitType) {
        super (parent);

        registerLocalVariable (new LocalVariable (unitType, "this", false, false));
        registerLocalVariable (new LocalVariable (Types.fromClass (Environment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, false, false));
    }

}
