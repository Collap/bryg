package io.collap.bryg.unit;

import java.util.ArrayList;
import java.util.List;

public abstract class FragmentInfo {

    protected UnitType owner;
    protected String name;
    protected List<ParameterInfo> parameters;

    public FragmentInfo (String name) {
        this.name = name;
        this.parameters = new ArrayList<> ();
    }

    public FragmentInfo (String name, List<ParameterInfo> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName () {
        return name;
    }

    public void addParameter (ParameterInfo parameter) {
        parameters.add (parameter);
    }

    /**
     * @return Local parameters expected by the fragment, excluding general parameters (if applicable).
     */
    public List<ParameterInfo> getLocalParameters () {
        return parameters;
    }

    public List<ParameterInfo> getGeneralParameters () {
        return new ArrayList<> (0);
    }

    /**
     * This method should be overridden.
     * @return All parameters expected by the fragment, including general parameters (if applicable).
     */
    public List<ParameterInfo> getAllParameters () {
        return parameters;
    }

    public UnitType getOwner () {
        return owner;
    }

    public void setOwner (UnitType owner) {
        this.owner = owner;
    }

}
