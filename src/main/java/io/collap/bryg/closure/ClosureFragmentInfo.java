package io.collap.bryg.closure;

import io.collap.bryg.unit.FragmentInfo;
import io.collap.bryg.unit.ParameterInfo;

import java.util.List;

public class ClosureFragmentInfo extends FragmentInfo {

    public ClosureFragmentInfo (String name) {
        super (name);
    }

    public ClosureFragmentInfo (String name, List<ParameterInfo> parameters) {
        super (name, parameters);
    }

}
