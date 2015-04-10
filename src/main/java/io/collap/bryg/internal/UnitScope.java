package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.util.List;

public class UnitScope extends Scope {

    /**
     * Automatically adds fields for __environment.
     */
    public UnitScope(@Nullable List<FieldInfo> fields) {
        super(null);

        registerInstanceVariable(new InstanceVariable(Types.fromClass(StandardEnvironment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, Mutability.immutable, Nullness.notnull));

        if (fields != null) {
            for (FieldInfo field : fields) {
                System.out.println("Register field: " + field.getName());
                registerInstanceVariable(new InstanceVariable(field));
            }
        }
    }

    public void registerInstanceVariable(InstanceVariable variable) {
        putVariable(variable);
    }

    public CompiledVariable getEnvironmentField() {
        @Nullable CompiledVariable variable = getVariable(StandardUnit.ENVIRONMENT_FIELD_NAME);
        if (variable == null) {
            throw new IllegalStateException("The " + StandardUnit.ENVIRONMENT_FIELD_NAME + " field is not declared.");
        }

        return variable;
    }

}
