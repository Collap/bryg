package io.collap.bryg.internal;

import io.collap.bryg.parser.BrygParser;

import java.util.List;
import java.util.Set;

public class TemplateCompilationData {

    private List<BrygParser.InDeclarationContext> fieldContexts;
    private List<FragmentCompileInfo> fragmentCompileInfos;

    /**
     * We need to ensure that all templates that are referenced by this template are actually compiled before the
     * current template is executed. This does not lead to any circular dependencies, as said templates are compiled
     * after this template has been registered in the class cache already.
     *
     * This set is not correctly filled until AFTER the compilation of the current template.
     */
    private Set<TemplateType> referencedTemplates;

    public TemplateCompilationData(List<FragmentCompileInfo> fragmentCompileInfos,
                                   List<BrygParser.InDeclarationContext> fieldContexts,
                                   Set<TemplateType> referencedTemplates) {
        this.fragmentCompileInfos = fragmentCompileInfos;
        this.fieldContexts = fieldContexts;
        this.referencedTemplates = referencedTemplates;
    }

    public List<FragmentCompileInfo> getFragmentCompileInfos() {
        return fragmentCompileInfos;
    }

    public List<BrygParser.InDeclarationContext> getFieldContexts() {
        return fieldContexts;
    }

    public Set<TemplateType> getReferencedTemplates() {
        return referencedTemplates;
    }

}
