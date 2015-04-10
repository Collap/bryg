package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class FragmentScope extends FunctionScope {

    public FragmentScope(UnitScope parent, UnitType unitType, @Nullable List<ParameterInfo> parameters) {
        super(parent, unitType, prependImplicitParameters(parameters));
    }

    private static List<ParameterInfo> prependImplicitParameters(@Nullable List<ParameterInfo> parameters) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }

        parameters.add(0, new ParameterInfo(Types.fromClass(Writer.class), "writer",
                Mutability.immutable, Nullness.notnull, null));
        return parameters;
    }

    public CompiledVariable getWriterVariable() {
        return getMandatoryVariable("writer");
    }

}
