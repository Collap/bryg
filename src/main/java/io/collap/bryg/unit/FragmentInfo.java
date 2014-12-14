package io.collap.bryg.unit;

import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.Types;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class FragmentInfo {

    protected UnitType owner;
    protected String name;
    protected List<VariableInfo> localParameters;
    protected String desc;

    public FragmentInfo (String name) {
        this.name = name;
        this.localParameters = new ArrayList<> ();
        generateDesc ();
    }

    public FragmentInfo (String name, List<VariableInfo> localParameters) {
        this.name = name;
        this.localParameters = localParameters;
        generateDesc ();
    }

    private void generateDesc () {
        int size = 1 + localParameters.size ();
        Type[] parameterTypes = new Type[size];
        parameterTypes[0] = Types.fromClass (Writer.class);
        for (int i = 1; i < size; ++i) {
            parameterTypes[i] = localParameters.get (i - 1).getType ();
        }

        desc = TypeHelper.generateMethodDesc (parameterTypes, Types.fromClass (Void.TYPE));
    }

    public String getName () {
        return name;
    }

    public void addParameter (VariableInfo parameter) {
        localParameters.add (parameter);
    }

    /**
     * @return Local parameters expected by the fragment, excluding general parameters (if applicable).
     */
    public List<VariableInfo> getLocalParameters () {
        return localParameters;
    }

    public List<VariableInfo> getGeneralParameters () {
        return new ArrayList<> (0);
    }

    /**
     * This method should be overridden.
     * @return All parameters expected by the fragment, including general parameters (if applicable).
     */
    public List<VariableInfo> getAllParameters () {
        return localParameters;
    }

    public UnitType getOwner () {
        return owner;
    }

    public void setOwner (UnitType owner) {
        this.owner = owner;
    }

    public String getDesc () {
        return desc;
    }

}
