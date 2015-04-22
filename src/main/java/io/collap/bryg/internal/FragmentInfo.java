package io.collap.bryg.internal;

import java.util.List;

public class FragmentInfo extends FunctionInfo {

    public final static String DIRECT_CALL_PREFIX = "__direct_";

    private String directName;

    public FragmentInfo(UnitType owner, FragmentCompileInfo compileInfo) {
        this(owner, compileInfo.getName(), compileInfo.getParameters());
    }

    public FragmentInfo(UnitType owner, String name, List<ParameterInfo> parameters) {
        super(owner, name, parameters);
        initializeDirectName(name);
    }

    private void initializeDirectName(String delegatorName) {
        this.directName = DIRECT_CALL_PREFIX + delegatorName;
    }

    public String getDirectName() {
        return directName;
    }

}
