package io.collap.bryg.internal;

import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class FragmentCompileInfo {

    private String name;
    private boolean isDefault;
    private List<ParameterInfo> parameters;
    private List<BrygParser.StatementContext> statementContexts;

    public FragmentCompileInfo(String name, boolean isDefault, List<ParameterInfo> parameters, List<BrygParser.StatementContext> statementContexts) {
        this.name = name;
        this.isDefault = isDefault;
        this.parameters = parameters;
        this.statementContexts = statementContexts;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<BrygParser.StatementContext> getStatementContexts () {
        return statementContexts;
    }

}
