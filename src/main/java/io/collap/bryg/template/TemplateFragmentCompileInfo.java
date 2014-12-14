package io.collap.bryg.template;

import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class TemplateFragmentCompileInfo {

    private TemplateFragmentInfo fragmentInfo;
    private List<BrygParser.StatementContext> statementContexts;

    public TemplateFragmentCompileInfo (TemplateFragmentInfo fragmentInfo, List<BrygParser.StatementContext> statementContexts) {
        this.fragmentInfo = fragmentInfo;
        this.statementContexts = statementContexts;
    }

    public TemplateFragmentInfo getFragmentInfo () {
        return fragmentInfo;
    }

    public List<BrygParser.StatementContext> getStatementContexts () {
        return statementContexts;
    }

}
