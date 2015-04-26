package io.collap.bryg.internal;

import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FunctionInfo {

    protected UnitType owner;
    protected String name;
    protected List<ParameterInfo> parameters;
    protected String desc;

    public FunctionInfo(UnitType owner, String name, @Nullable List<ParameterInfo> parameters) {
        this.owner = owner;
        this.name = name;

        if (parameters != null) {
            this.parameters = parameters;
        } else {
            this.parameters = new ArrayList<>();
        }
        generateDesc();
    }

    private void generateDesc() {
        desc = TypeHelper.generateMethodDesc(
                parameters.stream().map(VariableInfo::getType).toArray(Type[]::new),
                Types.fromClass(Void.TYPE)
        );
    }

    public UnitType getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    /**
     * @return Returns an immutable view of the parameters.
     */
    public List<ParameterInfo> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public String getDesc() {
        return desc;
    }

}
