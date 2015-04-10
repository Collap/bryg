package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import io.collap.bryg.Template;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.type.*;

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
            compileConstructor(classVisitor, unitType, true);
            compileConstructorDelegator(classVisitor, unitScope);

            for (FragmentCompileInfo compileInfo : unitType.getCompilationData().getFragmentCompileInfos()) {
                FragmentInfo fragmentInfo = unitType.getFragment(compileInfo.getName());
                compileFragment(classVisitor, fragmentInfo, compileInfo, unitScope);
                compileFragmentDelegator(classVisitor, fragmentInfo, unitScope);
            }
        }
        classVisitor.visitEnd();
    }

}
