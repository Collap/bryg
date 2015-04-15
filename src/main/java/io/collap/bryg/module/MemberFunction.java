package io.collap.bryg.module;

import io.collap.bryg.internal.MemberFunctionCallInfo;
import io.collap.bryg.internal.ParameterInfo;

import java.util.List;

public abstract class MemberFunction implements Member<MemberFunctionCallInfo> {

    protected String name;

    public MemberFunction(String name) {
        this.name = name;
    }

    /**
     * TODO: Comment.
     */
    public abstract List<ParameterInfo> getParameters();

    @Override
    public String getName() {
        return name;
    }

}
