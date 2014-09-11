package io.collap.bryg.compiler;

import io.collap.bryg.StandardTemplate;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.ast.RootNode;
import io.collap.bryg.compiler.bytecode.BrygClassVisitor;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.library.Library;
import io.collap.bryg.compiler.parser.DebugVisitor;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.GlobalVariableModel;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import bryg.org.objectweb.asm.ClassVisitor;
import bryg.org.objectweb.asm.ClassWriter;
import bryg.org.objectweb.asm.MethodVisitor;
import bryg.org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

import static bryg.org.objectweb.asm.Opcodes.*;

public class StandardCompiler implements Compiler {

    private Configuration configuration;
    private Library library;
    private ClassResolver classResolver;
    private GlobalVariableModel globalVariableModel;

    /**
     * Creates a StandardCompiler with an empty GlobalVariableModel.
     */
    public StandardCompiler (Configuration configuration, Library library, ClassResolver classResolver) {
        this (configuration, library, classResolver, new GlobalVariableModel ());
    }

    public StandardCompiler (Configuration configuration, Library library, ClassResolver classResolver,
                             GlobalVariableModel globalVariableModel) {
        this.configuration = configuration;
        this.library = library;
        this.classResolver = classResolver;
        this.globalVariableModel = globalVariableModel;
    }

    @Override
    public byte[] compile (String name, String source) {
        long parseStart = System.nanoTime ();

        boolean usedSLL = false;
        BrygParser.StartContext startContext = null;
        InputStream stream = new ByteArrayInputStream (source.getBytes ());
        try {
            BrygLexer lexer = new BrygLexer (new ANTLRInputStream (stream));
            CommonTokenStream tokenStream = new CommonTokenStream (lexer);

            if (configuration.shouldPrintTokens ()) {
                while (true) {
                    Token token = tokenStream.LT (1);
                    if (token.getType () == -1 /* EOF */) break;
                    else {
                        System.out.println (token);
                    }
                    tokenStream.consume ();
                }

                tokenStream.reset ();
            }

            /* Try with SLL(*). */
            BrygParser parser = new BrygParser (tokenStream);
            parser.getInterpreter ().setPredictionMode (PredictionMode.SLL);
            parser.removeErrorListeners ();
            parser.setErrorHandler (new BailErrorStrategy ());
            try {
                startContext = parser.start ();
                usedSLL = true;
            } catch (ParseCancellationException ex) {
                if (ex.getCause () instanceof RecognitionException) {
                    /* Try again with LL(*). */
                    tokenStream.reset ();
                    parser.addErrorListener (ConsoleErrorListener.INSTANCE);
                    parser.addErrorListener (new DiagnosticErrorListener ());
                    parser.setErrorHandler(new DefaultErrorStrategy());

                    parser.getInterpreter ().setPredictionMode (PredictionMode.LL);

                    startContext = parser.start ();
                }
            }
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle.
        }

        if (startContext == null) return null;

        if (configuration.shouldPrintParseTree ()) {
            PrintTreeVisitor printTreeVisitor = new PrintTreeVisitor ();
            printTreeVisitor.visit (startContext);
        }

        double parseTime = (System.nanoTime () - parseStart) / 1.0e9;

        long jitStart = System.nanoTime ();

        ClassWriter classWriter = new ClassWriter (ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parentVisitor;
        if (configuration.shouldPrintBytecode ()) {
            parentVisitor = new TraceClassVisitor (classWriter, new PrintWriter (System.out));
        }else {
            parentVisitor = classWriter;
        }
        BrygClassVisitor brygClassVisitor = new BrygClassVisitor (parentVisitor);
        compile (brygClassVisitor, name, startContext);

        double jitTime = (System.nanoTime () - jitStart) / 1.0e9;

        System.out.println ("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                " took " + parseTime + "s.");
        System.out.println ("The JIT took " + jitTime + "s.");

        return classWriter.toByteArray ();
    }

    private void compile (ClassVisitor classVisitor, String name, BrygParser.StartContext startContext) {
        classVisitor.visit (
                V1_7, ACC_PUBLIC, name.replace ('.', '/'), null,
                AsmTypes.getAsmType (StandardTemplate.class).getInternalName (),
                null);
        {
            createConstructor (classVisitor);

            BrygMethodVisitor render = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC, "render",
                    TypeHelper.generateMethodDesc (
                            new Class<?>[] { Writer.class, Model.class },
                            Void.TYPE
                    ),
                    null,
                    new String[] { AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName () });
            {
                Context context = new Context (render, library, classResolver, globalVariableModel);
                Node node = context.getParseTreeVisitor ().visit (startContext);

                if (configuration.shouldPrintAst ()) {
                    node.print (System.out, 0);
                }

                if (node instanceof RootNode) {
                    ((RootNode) node).addGlobalVariableInDeclarations (context.getRootScope ());
                }

                node.compile ();

                render.voidReturn ();
                render.visitMaxs (0, 0); /* Note: This function is called so the maximum values are calculated by ASM.
                                                  The arguments 0 and 0 have no special meaning. */
            }
            render.visitEnd ();
        }
        classVisitor.visitEnd ();
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

}
