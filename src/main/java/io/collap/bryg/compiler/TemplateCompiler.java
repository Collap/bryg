package io.collap.bryg.compiler;

import bryg.org.objectweb.asm.ClassVisitor;
import bryg.org.objectweb.asm.ClassWriter;
import bryg.org.objectweb.asm.MethodVisitor;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.compiler.ast.RootNode;
import io.collap.bryg.compiler.bytecode.BrygClassVisitor;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.scope.RootScope;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.template.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class TemplateCompiler implements Compiler<TemplateType> {

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
        String name = templateType.getFullName ();

        classVisitor.visit (
                V1_7, ACC_PUBLIC, TypeHelper.toInternalName (name), null,
                new Type (StandardTemplate.class).getAsmType ().getInternalName (),
                new String[] { new Type (Template.class).getAsmType ().getInternalName () });
        {
            createConstructor (classVisitor);

            for (TemplateFragmentCompileInfo compileInfo : templateType.getTemplateFragmentCompileInfos ()) {
                compileFragment (classVisitor, compileInfo);
            }
        }
        classVisitor.visitEnd ();
    }

    private void compileFragment (ClassVisitor classVisitor, TemplateFragmentCompileInfo compileInfo) {
        BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC,
                compileInfo.getFragmentInfo ().getName (),
                TypeHelper.generateMethodDesc (
                        new Class<?>[] { Writer.class, Model.class },
                        Void.TYPE
                ),
                null,
                new String[] { AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName () });
        {
            Context context = new Context (environment, compileInfo.getFragmentInfo (), templateType,
                    mv, new RootScope (environment.getGlobalVariableModel ()));

            /* Add general parameters to input declarations. */
            List<BrygParser.InDeclarationContext> inDeclarationContexts;
            inDeclarationContexts = new ArrayList<> ();
            inDeclarationContexts.addAll (templateType.getGeneralParameterContexts ());
            inDeclarationContexts.addAll (compileInfo.getInDeclarationContexts ());

            RootNode node = new RootNode (context, inDeclarationContexts, compileInfo.getStatementContexts ());

            if (environment.getConfiguration ().shouldPrintAst ()) {
                node.print (System.out, 0);
                System.out.println ();
            }

            node.addGlobalVariableInDeclarations (context.getRootScope ());
            node.compile ();

            mv.voidReturn ();
            mv.visitMaxs (0, 0); /* Note: This function is called so the maximum values are calculated by ASM.
                                                  The arguments 0 and 0 have no special meaning. */
        }
        mv.visitEnd ();
    }

    private void createConstructor (ClassVisitor classVisitor) {
        String desc = TypeHelper.generateMethodDesc (new Class[] {Environment.class }, Void.TYPE);
        MethodVisitor constructor = classVisitor.visitMethod (ACC_PUBLIC, "<init>",
                desc,
                null, null);
        constructor.visitVarInsn (ALOAD, 0); /* this */
        constructor.visitVarInsn (ALOAD, 1); /* environment */
        constructor.visitMethodInsn (INVOKESPECIAL,
                AsmTypes.getAsmType (StandardTemplate.class).getInternalName (),
                "<init>", desc, false);
        constructor.visitInsn (RETURN);
        constructor.visitMaxs (2, 2);
        constructor.visitEnd ();
    }

    @Override
    public TemplateType getUnitType () {
        return templateType;
    }

}
