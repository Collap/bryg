package io.collap.bryg.template;

import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class TemplateFragmentCompileInfo {

    private TemplateFragmentInfo fragmentInfo;
    private List<BrygParser.InDeclarationContext> inDeclarationContexts;
    private List<BrygParser.StatementContext> statementContexts;

    public TemplateFragmentCompileInfo (TemplateFragmentInfo fragmentInfo, List<BrygParser.InDeclarationContext> inDeclarationContexts, List<BrygParser.StatementContext> statementContexts) {
        this.fragmentInfo = fragmentInfo;
        this.inDeclarationContexts = inDeclarationContexts;
        this.statementContexts = statementContexts;
    }

    public TemplateFragmentInfo getFragmentInfo () {
        return fragmentInfo;
    }

    public List<BrygParser.InDeclarationContext> getInDeclarationContexts () {
        return inDeclarationContexts;
    }

    public List<BrygParser.StatementContext> getStatementContexts () {
        return statementContexts;
    }

}
