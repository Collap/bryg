package io.collap.bryg.internal;

import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class FragmentCompileInfo {

    private String name;
    private List<ParameterInfo> parameters;
    private List<BrygParser.StatementContext> statementContexts;

    public FragmentCompileInfo(String name, List<ParameterInfo> parameters, List<BrygParser.StatementContext> statementContexts) {
        this.name = name;
        this.parameters = parameters;
        this.statementContexts = statementContexts;
    }

    public String getName() {
        return name;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<BrygParser.StatementContext> getStatementContexts () {
        return statementContexts;
    }

}
