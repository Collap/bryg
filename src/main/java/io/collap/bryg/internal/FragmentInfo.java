package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.Types;

import java.io.Writer;
import java.util.List;

// TODO: Delegators should have the prefix, not the direct methods.

public class FragmentInfo extends FunctionInfo {

    public final static String DIRECT_CALL_PREFIX = "__direct_";

    private String directName;
    private boolean isDefault;

    public FragmentInfo(UnitType owner, FragmentCompileInfo compileInfo) {
        this(owner, compileInfo.getName(), compileInfo.isDefault(), compileInfo.getParameters());
    }

    public FragmentInfo(UnitType owner, String name, boolean isDefault, List<ParameterInfo> parameters) {
        super(owner, name, addWriterParameter(parameters));
        initializeDirectName(name);
        this.isDefault = isDefault;
    }

    private static List<ParameterInfo> addWriterParameter(List<ParameterInfo> parameters) {
        parameters.add(0, new ParameterInfo(Types.fromClass(Writer.class), "writer",
                Mutability.immutable, Nullness.notnull, null));
        return parameters;
    }

    private void initializeDirectName(String delegatorName) {
        this.directName = DIRECT_CALL_PREFIX + delegatorName;
    }

    public String getDirectName() {
        return directName;
    }

    public boolean isDefault() {
        return isDefault;
    }

}
