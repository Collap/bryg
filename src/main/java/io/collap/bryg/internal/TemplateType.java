package io.collap.bryg.internal;

import io.collap.bryg.*;
import io.collap.bryg.internal.compiler.util.IdUtil;
import io.collap.bryg.internal.type.TypeInterpreter;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class TemplateType extends UnitType {

    /**
     * May be null, in which case the template has not been compiled yet.
     */
    private @Nullable Class<? extends Template> templateClass = null;
    private @Nullable Constructor<? extends Template> constructor = null;

    /**
     * This information can be used when the template type has been created, but the
     * template has not been fully compiled. It is set to null after compilation.
     */
    private @Nullable TemplateCompilationData compilationData;

    /**
     * Also extracts fragments from the compile infos and adds them to the UnitType.
     */
    public TemplateType(String className, TemplateCompilationData compilationData, ClassResolver classResolver) {
        super(className);
        this.compilationData = compilationData;
        configureFragments();
        configureFields(classResolver);
        configureConstructorInfo();
    }

    private void configureFragments() {
        if (compilationData == null) {
            throw new IllegalStateException("Compilation data is null.");
        }

        for (FragmentCompileInfo compileInfo : compilationData.getFragmentCompileInfos()) {
            addFragment(new FragmentInfo(this, compileInfo));
        }
    }

    private void configureFields(ClassResolver classResolver) {
        if (compilationData == null) {
            throw new IllegalStateException("Compilation data is null.");
        }

        for (BrygParser.FieldDeclarationContext fieldContext : compilationData.getFieldContexts()) {
            addField(
                    new FieldInfo(
                            new TypeInterpreter(classResolver).interpretType(fieldContext.type()),
                            IdUtil.idToString(fieldContext.id()),
                            Mutability.immutable,
                            Nullness.notnull
                    )
            );
        }
    }

    @Override
    protected void addConstructorParameters(List<ParameterInfo> parameters) {
        // Nothing to do here.
    }

    @Override
    public Class<?> getStandardUnitClass() {
        return StandardTemplate.class;
    }

    @Override
    public TemplateType getParentTemplateType() {
        return this;
    }

    @Override
    public FragmentInfo getFragment(String name) {
        return super.getFragment(name);
    }

    /**
     * Clears the compilation data.
     * Use AFTER the template has been compiled, otherwise the backlash will be horrible, horrible.
     */
    public void clearCompilationData() {
        compilationData = null;
    }

    /**
     * Returns true after the compiler has successfully compiled the template and set the class.
     * This does not, however, entail that the template has already been registered at the Environment,
     * or even will be in the future.
     */
    public boolean isCompiled() {
        return templateClass != null;
    }

    public @Nullable Class<? extends Template> getTemplateClass() {
        return templateClass;
    }

    public void setTemplateClass(Class<? extends Template> templateClass) {
        this.templateClass = templateClass;
    }

    public Constructor<? extends Template> getPublicConstructor() {
        if (templateClass == null) {
            throw new IllegalStateException("A constructor was requested without a compiled template class being present.");
        }

        // This is lazily loaded, because at this point the JVM actually checks the class,
        // which is definitely not what we want when we compile templates which depend on
        // each other.
        if (constructor == null) {
            try {
                constructor = templateClass.getConstructor(Environment.class, Model.class);
            } catch (NoSuchMethodException e) {
                throw new CompilationException("Constructor of template " + fullName + " could not be loaded!", e);
            }
        }

        return constructor;
    }

    public TemplateCompilationData getCompilationData() {
        if (compilationData == null) {
            throw new IllegalStateException("Compilation data was requested after having been cleaned up.");
        }

        return compilationData;
    }

}
