package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import io.collap.bryg.CompilationException;
import io.collap.bryg.Template;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.type.*;

import javax.annotation.Nullable;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Compile dynamic init function? (This TODO is probably obsolete, check!)

public class TemplateCompiler extends UnitCompiler<TemplateType> {

    private StandardEnvironment environment;

    public TemplateCompiler(StandardEnvironment environment, TemplateType templateType) {
        super(environment, templateType);
        this.environment = environment;
    }

    @Override
    protected void compileClass(ClassVisitor classVisitor) {
        classVisitor.visit(
                V1_7, ACC_PUBLIC, TypeHelper.toInternalName(unitType.getFullName()), null,
                Types.fromClass(StandardTemplate.class).getInternalName(),
                new String[]{Types.fromClass(Template.class).getInternalName()});
        {
            classVisitor.visitSource(unitType.getSimpleName() + ".bryg", null);
            compileFields(classVisitor, unitType.getFields());

            UnitScope unitScope = new UnitScope(unitType.getFields());
            compileConstructor(classVisitor, unitType);
            compileConstructorDelegator(classVisitor, unitScope);

            for (FragmentCompileInfo compileInfo : unitType.getCompilationData().getFragmentCompileInfos()) {
                @Nullable FragmentInfo fragmentInfo = unitType.getFragment(compileInfo.getName());
                if (fragmentInfo == null) {
                    throw new CompilationException("The fragment " + compileInfo.getName() + " does not exist, " +
                            "despite being listed in the unit's compilation data.");
                }
                compileFragment(classVisitor, fragmentInfo, compileInfo, unitScope);
                compileFragmentDelegator(classVisitor, fragmentInfo.getName(), fragmentInfo, unitScope);
                if (compileInfo.isDefault() && !fragmentInfo.getName().equals(UnitType.DEFAULT_FRAGMENT_NAME)) {
                    // TODO: Optimize by calling the OTHER delegator.
                    compileFragmentDelegator(classVisitor, UnitType.DEFAULT_FRAGMENT_NAME, fragmentInfo, unitScope);
                }
            }
        }
        classVisitor.visitEnd();
    }

}
