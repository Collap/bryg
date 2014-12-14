package io.collap.bryg.compiler.scope;

import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.unit.StandardUnit;

import javax.annotation.Nullable;
import java.util.List;

public class UnitScope extends Scope {

    /**
     * Automatically adds fields for __environment and __globals.
     */
    public UnitScope (@Nullable List<VariableInfo> fields) {
        super (null);

        registerInstanceVariable (new InstanceVariable (Types.fromClass (Environment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, false, false));
        registerInstanceVariable (new InstanceVariable (Types.fromClass (GlobalVariableModel.class),
                StandardUnit.GLOBALS_FIELD_NAME, false, false));

        if (fields != null) {
            for (VariableInfo field : fields) {
                System.out.println ("Register field: " + field.getName ());
                registerInstanceVariable (new InstanceVariable (field.getType (), field.getName (),
                        false, field.isNullable ()));
            }
        }
    }

    @Override
    public LocalVariable registerLocalVariable (LocalVariable variable) {
        throw new UnsupportedOperationException ("A variable registered at a unit scope may not be local.");
    }

    @Override
    public void registerVariableInternal (Variable variable) {
        if (!(variable instanceof InstanceVariable)) {
            throw new IllegalArgumentException ("A variable registered at a unit scope must be an instance variable.");
        }
        super.registerVariableInternal (variable);
    }

}
