package io.collap.bryg;

import java.util.ArrayList;
import java.util.List;

public class TemplateInfo {

    private List<ParameterInfo> parameters = new ArrayList<> ();

    public void addParameter (ParameterInfo parameter) {
        parameters.add (parameter);
    }

    public List<ParameterInfo> getParameters () {
        return parameters;
    }

}
