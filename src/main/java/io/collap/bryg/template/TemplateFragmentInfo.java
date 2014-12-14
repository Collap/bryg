package io.collap.bryg.template;

import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.unit.FragmentInfo;

import java.util.ArrayList;
import java.util.List;

public class TemplateFragmentInfo extends FragmentInfo {

    public final static String DIRECT_CALL_PREFIX = "__direct_";

    private List<VariableInfo> allParameters;
    private String delegatorName;

    public TemplateFragmentInfo (String delegatorName) {
        super (DIRECT_CALL_PREFIX + delegatorName);
        this.delegatorName = delegatorName;
    }

    public TemplateFragmentInfo (String delegatorName, List<VariableInfo> parameters) {
        super (DIRECT_CALL_PREFIX + delegatorName, parameters);
        this.delegatorName = delegatorName;
    }

    @Override
    public List<VariableInfo> getAllParameters () {
        if (allParameters == null) {
            List<VariableInfo> generalParameters = ((TemplateType) owner).getGeneralParameters ();
            allParameters = new ArrayList<> (generalParameters.size () + localParameters.size ());
            allParameters.addAll (generalParameters);
            allParameters.addAll (localParameters);
        }
        return allParameters;
    }

    @Override
    public List<VariableInfo> getGeneralParameters () {
        return ((TemplateType) owner).getGeneralParameters ();
    }

    @Override
    public TemplateType getOwner () {
        return (TemplateType) super.getOwner ();
    }

    public String getDelegatorName () {
        return delegatorName;
    }

}
