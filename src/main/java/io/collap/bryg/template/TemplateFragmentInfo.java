package io.collap.bryg.template;

import io.collap.bryg.Closure;
import io.collap.bryg.BrygJitException;
import io.collap.bryg.internal.ParameterInfo;
import io.collap.bryg.internal.FragmentInfo;

import java.util.ArrayList;
import java.util.List;

public class TemplateFragmentInfo extends FragmentInfo {

    public final static String DIRECT_CALL_PREFIX = "__direct_";

    private List<ParameterInfo> allParameters;
    private String directName;

    public TemplateFragmentInfo(String delegatorName) {
        super(delegatorName);
        this.directName = DIRECT_CALL_PREFIX + delegatorName;
    }

    public TemplateFragmentInfo(String delegatorName, List<VariableInfo> parameters) {
        super(delegatorName, parameters);
        this.directName = DIRECT_CALL_PREFIX + delegatorName;
        checkParameters();
    }

    /**
     * Enforces that there is only one Closure parameter at the end of the parameter list.
     */
    private void checkParameters() {
        List<VariableInfo> params = localParameters;
        for (int i = 0; i < params.size() - 1; ++i) {
            if (params.get(i).getType().similarTo(Closure.class)) {
                throw new BrygJitException("The fragment either has a Closure parameter that is not the last parameter, " +
                        "or the fragment has multiple Closure parameters.", -1);
            }
        }
    }

    @Override
    public List<VariableInfo> getAllParameters() {
        if (allParameters == null) {
            List<VariableInfo> generalParameters = ((TemplateType) owner).getGeneralParameters();
            allParameters = new ArrayList<>(generalParameters.size() + localParameters.size());
            allParameters.addAll(generalParameters);
            allParameters.addAll(localParameters);
        }
        return allParameters;
    }

    @Override
    public List<VariableInfo> getGeneralParameters() {
        return ((TemplateType) owner).getGeneralParameters();
    }

    @Override
    public TemplateType getOwner() {
        return (TemplateType) super.getOwner();
    }

    public String getDirectName() {
        return directName;
    }

    @Override
    public String getName() {
        return super.getName();
    }

}
