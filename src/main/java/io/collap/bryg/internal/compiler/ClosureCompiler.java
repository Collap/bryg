package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import io.collap.bryg.Closure;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.type.*;
import io.collap.bryg.Model;

import java.io.Writer;

import static bryg.org.objectweb.asm.Opcodes.*;

public class ClosureCompiler extends UnitCompiler<ClosureType> {

    private ClosureScope closureScope;

    public ClosureCompiler(StandardEnvironment environment, ClosureType closureType, ClosureScope closureScope) {
        super(environment, closureType);
        this.closureScope = closureScope;
    }

    @Override
    protected void compileClass(ClassVisitor classVisitor) {
        classVisitor.visit(
                V1_7, ACC_PUBLIC, TypeHelper.toInternalName(unitType.getFullName()), null,
                Types.fromClass(StandardUnit.class).getInternalName(),
                new String[]{Types.fromClass(Closure.class).getInternalName()});
        {
            classVisitor.visitSource(unitType.getParentTemplateType().getSimpleName() + ".bryg", null);

            // We need to start with the compilation of the default fragment right away, since
            // the compilation context is required to parse the nodes. And only then are we
            // able to know the fields that this closure has, since fields represent the captured
            // variables.
            FragmentInfo fragmentInfo = unitType.getFragment(UnitType.DEFAULT_FRAGMENT_NAME);
            FragmentScope fragmentScope = new FragmentScope(closureScope, unitType, fragmentInfo.getParameters());

            CompilationContext compilationContext = new CompilationContext(environment, fragmentInfo, unitType, null,
                    fragmentScope, closureScope);

            // Also finds all variables that need to be captured!
            // Which are added as fields by the closure scope automatically.
            Node node = compilationContext.getParseTreeVisitor().visit(unitType.getClosureContext());

            // Make sure the node is created before these methods are called, so every captured variable
            // is correctly turned into a field and constructor parameter.
            compileFields(classVisitor, unitType.getFields());
            compileConstructor(classVisitor, unitType, true);

            BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC, fragmentInfo.getName(),
                    TypeHelper.generateMethodDesc(
                            new Class<?>[]{Writer.class, Model.class},
                            Void.TYPE
                    ), null, null);
            {
                compilationContext.setMethodVisitor(mv);

                if (environment.getDebugConfiguration().shouldPrintAst()) {
                    node.print(System.out, 0);
                    System.out.println();
                }

                node.compile();

                mv.voidReturn();
                mv.visitMaxsAuto();
            }
            mv.visitEnd();

            // Also compile the delegator to allow calling closures when no parameter information is available.
            compileFragmentDelegator(classVisitor, fragmentInfo, closureScope);
        }
        classVisitor.visitEnd();
    }

}
