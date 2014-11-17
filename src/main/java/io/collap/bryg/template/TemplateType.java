package io.collap.bryg.template;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.FragmentInfo;
import io.collap.bryg.unit.ParameterInfo;
import io.collap.bryg.unit.StandardUnit;
import io.collap.bryg.unit.UnitType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateType extends UnitType {

    /**
     * May be null, in which case the template has not been compiled yet.
     */
    private Class<? extends Template> templateClass;
    private Constructor<? extends Template> constructor;

    private List<ParameterInfo> generalParameters;

    /* These fields can be used when the template type has been created, but the
       template has not been fully compiled. Is set to null after compilation. */

    private List<TemplateFragmentCompileInfo> templateFragmentCompileInfos;
    private List<BrygParser.InDeclarationContext> generalParameterContexts;

    /**
     * We need to ensure that all templates that are referenced by this template are actually compiled before the
     * current template is executed. This does not lead to any circular dependencies, as said templates are compiled
     * after this template has been registered in the class cache already.
     */
    private Set<TemplateType> referencedTemplates;

    public TemplateType (String className) {
        this (className, new ArrayList<ParameterInfo> (), new ArrayList<TemplateFragmentCompileInfo> (),
                new ArrayList<BrygParser.InDeclarationContext> ());
    }

    /**
     * Also extracts fragments from the compile infos and adds them to the UnitType.
     */
    public TemplateType (String className, List<ParameterInfo> generalParameters,
                         List<TemplateFragmentCompileInfo> templateFragmentCompileInfos,
                         List<BrygParser.InDeclarationContext> generalParameterContexts) {
        super (className);
        this.constructor = null;
        this.templateClass = null;
        this.generalParameters = generalParameters;
        this.templateFragmentCompileInfos = templateFragmentCompileInfos;
        this.generalParameterContexts = generalParameterContexts;
        this.referencedTemplates = new HashSet<> ();
        fillFragments ();
    }

    private void fillFragments () {
        for (TemplateFragmentCompileInfo compileInfo : templateFragmentCompileInfos) {
            addFragment (compileInfo.getFragmentInfo ());
        }
    }

    public TemplateType (String className, Class<? extends Template> templateClass) {
        super (className);
        this.templateClass = templateClass;
        this.constructor = null;
        this.templateFragmentCompileInfos = null;
    }

    /**
     * Clears the compilation data.
     * Use AFTER the template has been compiled, otherwise the backlash will be horrible, horrible.
     */
    public void clearCompilationData () {
        templateFragmentCompileInfos = null;
        generalParameterContexts = null;
        referencedTemplates = null;
    }

    public Constructor<? extends Template> getConstructor () {
        /* This is lazily loaded, because at this point the JVM actually checks the class,
           which is definitely not what we want when we compile templates which depend on
           each other. */
        if (constructor == null) {
            try {
                if (StandardUnit.class.isAssignableFrom (templateClass)) {
                    constructor = templateClass.getConstructor (Environment.class);
                } else {
                    constructor = templateClass.getConstructor ();
                }
            }catch (NoSuchMethodException e) {
                e.printStackTrace ();
                throw new RuntimeException ("Constructor of template " + fullName + " could not be loaded!");
            }
        }

        return constructor;
    }

    /**
     * Automatically adds the fragment to the UnitType.
     */
    public void addFragmentCompileInfo (TemplateFragmentCompileInfo compileInfo) {
        templateFragmentCompileInfos.add (compileInfo);
        addFragment (compileInfo.getFragmentInfo ());
    }

    @Override
    public void addFragment (FragmentInfo fragment) {
        fragment.setOwner (this);
        super.addFragment (fragment);
    }

    public String getTemplateInterfaceName () {
        return fullName + "$" + "Interface";
    }

    public List<TemplateFragmentCompileInfo> getTemplateFragmentCompileInfos () {
        return templateFragmentCompileInfos;
    }

    public void addGeneralParameter (ParameterInfo parameterInfo, BrygParser.InDeclarationContext ctx) {
        generalParameters.add (parameterInfo);
        generalParameterContexts.add (ctx);
    }

    public List<ParameterInfo> getGeneralParameters () {
        return generalParameters;
    }

    public List<BrygParser.InDeclarationContext> getGeneralParameterContexts () {
        return generalParameterContexts;
    }

    @Override
    public TemplateFragmentInfo getFragment (String name) {
        return (TemplateFragmentInfo) super.getFragment (name);
    }

    /* This set is not correctly filled until AFTER the compilation of the current template. */
    public Set<TemplateType> getReferencedTemplates () {
        return referencedTemplates;
    }

    @Override
    public TemplateType getParentTemplateType () {
        return this;
    }

    /**
     * Returns true after the compiler has successfully compiled the template and set the class.
     * This does not, however, entail that the template has already been registered at the Environment,
     * or even will be in the future.
     */
    public boolean isCompiled () {
        return templateClass != null;
    }

    public Class<? extends Template> getTemplateClass () {
        return templateClass;
    }

    public void setTemplateClass (Class<? extends Template> templateClass) {
        this.templateClass = templateClass;
    }

}
