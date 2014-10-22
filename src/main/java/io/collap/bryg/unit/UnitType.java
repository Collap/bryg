package io.collap.bryg.unit;

import java.util.ArrayList;
import java.util.List;

public abstract class UnitType {

    protected String fullName;
    protected String classPackage;
    protected List<ParameterInfo> parameters = new ArrayList<> ();

    public UnitType (String fullName) {
        this.fullName = fullName;

        /* Set unit package based on name. */
        int lastDot = fullName.lastIndexOf ('.');
        if (lastDot >= 0) {
            classPackage = fullName.substring (0, lastDot);
        }else {
            classPackage = "";
        }
    }

    public String getFullName () {
        return fullName;
    }

    public String getClassPackage () {
        return classPackage;
    }

    public void addParameter (ParameterInfo parameter) {
        parameters.add (parameter);
    }

    public List<ParameterInfo> getParameters () {
        return parameters;
    }

}
