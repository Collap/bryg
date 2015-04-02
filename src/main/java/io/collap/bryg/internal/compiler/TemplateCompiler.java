package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.Template;
import io.collap.bryg.internal.StandardTemplate;
import io.collap.bryg.internal.compiler.ast.AccessMode;
import io.collap.bryg.internal.compiler.ast.DelegatorRootNode;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.RootNode;
import io.collap.bryg.internal.compiler.ast.expression.ModelLoadExpression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.scope.*;
import io.collap.bryg.internal.type.*;
import io.collap.bryg.internal.Type;
import io.collap.bryg.Environment;
import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.InvalidInputParameterException;
import io.collap.bryg.Model;
import io.collap.bryg.template.*;
import io.collap.bryg.internal.FragmentInfo;
import io.collap.bryg.internal.StandardUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

// TODO: Compile dynamic init function? (This TODO is probably obsolete, check!)

public class TemplateCompiler extends UnitCompiler implements Compiler<TemplateType> {

    private StandardEnvironment environment;
    private TemplateType templateType;

    public TemplateCompiler (StandardEnvironment environment, TemplateType templateType) {
        this.environment = environment;
        this.templateType = templateType;
    }

    @Override
    public byte[] compile () {
        long jitStart = System.nanoTime ();

        ClassWriter classWriter = new ClassWriter (ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parentVisitor;
        if (environment.getConfiguration ().shouldPrintBytecode ()) {
            parentVisitor = new TraceClassVisitor (classWriter, new PrintWriter (System.out));
        }else {
            parentVisitor = classWriter;
        }
        BrygClassVisitor brygClassVisitor = new BrygClassVisitor (parentVisitor);
        compileClass (brygClassVisitor);
        if (environment.getConfiguration ().shouldPrintBytecode ()) {
            System.out.println ();
        }

        double jitTime = (System.nanoTime () - jitStart) / 1.0e9;

        System.out.println ("The JIT took " + jitTime + "s.");

        return classWriter.toByteArray ();
    }

    private void compileClass (ClassVisitor classVisitor) {
        classVisitor.visit (
                V1_7, ACC_PUBLIC, TypeHelper.toInternalName (templateType.getFullName ()), null,
                Types.fromClass (StandardTemplate.class).getInternalName (),
                new String[] { Types.fromClass (Template.class).getInternalName () });
        {
            classVisitor.visitSource (templateType.getSimpleName () + ".bryg", null);

            compileFields (classVisitor, templateType.getGeneralParameters ());

            List<VariableInfo> unitFields = new ArrayList<> ();
            unitFields.addAll (templateType.getGeneralParameters ());
            UnitScope unitScope = new UnitScope (unitFields);

            // compileConstructor (classVisitor, templateType, false, null);
            compileDelegatorConstructor (classVisitor, unitScope);
            compileConstructor (classVisitor, templateType, true, templateType.getGeneralParameters ());

            for (TemplateFragmentCompileInfo compileInfo : templateType.getTemplateFragmentCompileInfos ()) {
                compileFragment (classVisitor, compileInfo, unitScope);
                compileDelegator (classVisitor, compileInfo, unitScope);
            }
        }
        classVisitor.visitEnd ();
    }

    private void compileDelegatorConstructor (ClassVisitor classVisitor, UnitScope unitScope) {
        String desc = TypeHelper.generateMethodDesc (
                new Type[] { Types.fromClass (Environment.class), Types.fromClass (Model.class) },
                Types.fromClass (Void.TYPE)
        );
        BrygMethodVisitor constructor = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC, "<init>", desc, null, null);

        /* Call super. */
        constructor.visitVarInsn (ALOAD, 0); /* this */
        constructor.visitVarInsn (ALOAD, 1); /* environment */
        constructor.visitMethodInsn (INVOKESPECIAL,
                AsmTypes.getAsmType (StandardTemplate.class).getInternalName (),
                "<init>", TypeHelper.generateMethodDesc (
                        new Class[] { Environment.class },
                        Void.TYPE
                ), false);

        /* Load values from model and store them. */
        final ConstructorScope scope = new ConstructorScope (unitScope, templateType);
        final LocalVariable model = new LocalVariable (Types.fromClass (Model.class), "model", false);
        scope.registerLocalVariable (model);

        Context context = new Context (environment, new TemplateFragmentInfo ("<init>",
                new ArrayList<VariableInfo> () {{
                    add (scope.getVariable ("this").getInfo ());
                    add (scope.getVariable (StandardUnit.ENVIRONMENT_FIELD_NAME).getInfo ());
                    add (model.getInfo ());
                }}),
                templateType, constructor, scope);
        DelegatorRootNode root = new DelegatorRootNode (context);

        /* Load and store. */
        List<VariableInfo> generalParameters = templateType.getGeneralParameters ();
        for (VariableInfo parameter : generalParameters) {
            root.addChild (new VariableExpression (context, -1, scope.getVariable (parameter.getName ()),
                    AccessMode.set, new ModelLoadExpression (context, parameter, model)));
        }

        root.compile ();

        constructor.voidReturn ();
        constructor.visitMaxsAuto ();
        constructor.visitEnd ();
    }

    private void compileFragment (ClassVisitor classVisitor, TemplateFragmentCompileInfo compileInfo, UnitScope unitScope) {
        FragmentInfo fragmentInfo = compileInfo.getFragmentInfo ();

        BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC,
                compileInfo.getFragmentInfo ().getDirectName (), fragmentInfo.getDesc (), null, null);
        {
            MethodScope methodScope = new MethodScope (unitScope, templateType,
                    environment.getGlobalVariableModel (), fragmentInfo.getLocalParameters ());
            Context context = new Context (environment, fragmentInfo, templateType, mv, methodScope);

            RootNode node = new RootNode (context, compileInfo.getStatementContexts ());

            if (environment.getConfiguration ().shouldPrintAst ()) {
                node.print (System.out, 0);
                System.out.println ();
            }

            node.addGlobalVariableLoads (methodScope);
            node.compile ();

            mv.voidReturn ();
            mv.visitMaxsAuto ();
        }
        mv.visitEnd ();
    }

    private void compileDelegator (ClassVisitor classVisitor, TemplateFragmentCompileInfo compileInfo, UnitScope unitScope) {
        final TemplateFragmentInfo fragmentInfo = compileInfo.getFragmentInfo ();

        final BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC,
                fragmentInfo.getName (),
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { Writer.class, Model.class },
                        Void.TYPE
                ),
                null,
                new String[] { AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName () });
        {
            List<VariableInfo> parameters = fragmentInfo.getLocalParameters ();
            MethodScope scope = new MethodScope (unitScope, templateType, environment.getGlobalVariableModel (), null);
            Context context = new Context (environment, fragmentInfo, templateType,
                    mv, scope);
            LocalVariable model = new LocalVariable (Types.fromClass (Model.class), "model", false);
            scope.registerVariableInternal (model);
            Variable thisVar = scope.getVariable ("this");
            Variable writer = scope.getVariable ("writer");

            DelegatorRootNode root = new DelegatorRootNode (context);
            root.addChild (new VariableExpression (context, -1, thisVar, AccessMode.get));

            /* Add all parameters in order. */
            root.addChild (new VariableExpression (context, -1, writer, AccessMode.get));
            for (VariableInfo parameter : parameters) {
                root.addChild (new ModelLoadExpression (context, parameter, model));
            }

            /* Call the actual fragment function. */
            root.addChild (new Node (context) {
                @Override
                public void compile () {
                    mv.visitMethodInsn (INVOKEVIRTUAL, fragmentInfo.getOwner ().getInternalName (),
                            fragmentInfo.getDirectName (), fragmentInfo.getDesc (), false);
                }
            });

            if (environment.getConfiguration ().shouldPrintAst ()) {
                root.print (System.out, 0);
                System.out.println ();
            }

            root.compile ();

            mv.voidReturn ();
            mv.visitMaxsAuto ();
        }
        mv.visitEnd ();
    }

    @Override
    public TemplateType getUnitType () {
        return templateType;
    }

}
