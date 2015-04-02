package io.collap.bryg.internal.scope;

import io.collap.bryg.internal.type.Types;
import io.collap.bryg.Environment;
import io.collap.bryg.internal.StandardUnit;
import io.collap.bryg.internal.UnitType;

public class ConstructorScope extends HighestLocalScope {

    public ConstructorScope (UnitScope parent, UnitType unitType) {
        super (parent);

        registerLocalVariable (new LocalVariable (unitType, "this", false, false));
        registerLocalVariable (new LocalVariable (Types.fromClass (Environment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, false, false));
    }

}
