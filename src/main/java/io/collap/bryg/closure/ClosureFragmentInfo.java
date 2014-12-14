package io.collap.bryg.closure;

import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.unit.FragmentInfo;

import java.util.List;

public class ClosureFragmentInfo extends FragmentInfo {

    public ClosureFragmentInfo (String name) {
        super (name);
    }

    public ClosureFragmentInfo (String name, List<VariableInfo> parameters) {
        super (name, parameters);
    }

}
