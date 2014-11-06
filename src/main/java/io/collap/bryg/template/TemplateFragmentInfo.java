package io.collap.bryg.template;

import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.unit.FragmentInfo;
import io.collap.bryg.unit.ParameterInfo;
import io.collap.bryg.unit.UnitType;

import java.util.ArrayList;
import java.util.List;

public class TemplateFragmentInfo extends FragmentInfo {

    private List<ParameterInfo> allParameters;

    public TemplateFragmentInfo (String name) {
        super (name);
    }

    public TemplateFragmentInfo (String name, List<ParameterInfo> parameters) {
        super (name, parameters);
    }

    @Override
    public List<ParameterInfo> getAllParameters () {
        if (allParameters == null) {
            List<ParameterInfo> generalParameters = ((TemplateType) owner).getGeneralParameters ();
            allParameters = new ArrayList<> (generalParameters.size () + parameters.size ());
            allParameters.addAll (generalParameters);
            allParameters.addAll (parameters);
        }
        return allParameters;
    }

    @Override
    public List<ParameterInfo> getGeneralParameters () {
        return ((TemplateType) owner).getGeneralParameters ();
    }

    @Override
    public TemplateType getOwner () {
        return (TemplateType) super.getOwner ();
    }

}
