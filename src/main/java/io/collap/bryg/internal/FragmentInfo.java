package io.collap.bryg.internal;

import io.collap.bryg.Mutability;
import io.collap.bryg.Nullness;
import io.collap.bryg.internal.type.Types;

import java.io.Writer;
import java.util.List;

/**
 * The implicit writer parameter is added to the parameter list automatically.
 */
public class FragmentInfo extends FunctionInfo {

    public final static String DIRECT_CALL_PREFIX = "__direct_";

    private String directName;

    public FragmentInfo(TemplateType owner, FragmentCompileInfo compileInfo) {
        this(owner, compileInfo.getName(), compileInfo.getParameters());
    }

    public FragmentInfo(TemplateType owner, String name, List<ParameterInfo> parameters) {
        super(owner, name, addImplicitParameters(parameters));
        initializeDirectName(name);
    }

    private static List<ParameterInfo> addImplicitParameters(List<ParameterInfo> parameters) {
        parameters.add(0, new ParameterInfo(Types.fromClass(Writer.class), "writer", Mutability.immutable,
                Nullness.notnull, null));
        return parameters;
    }

    private void initializeDirectName(String delegatorName) {
        this.directName = DIRECT_CALL_PREFIX + delegatorName;
    }

    @Override
    public TemplateType getOwner() {
        return (TemplateType) super.getOwner();
    }

    public String getDirectName() {
        return directName;
    }

}
