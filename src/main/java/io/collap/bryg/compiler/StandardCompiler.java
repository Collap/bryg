package io.collap.bryg.compiler;

import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.library.BasicLibrary;
import io.collap.bryg.compiler.parser.BrygClassVisitor;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.DebugVisitor;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.compiler.preprocessor.Preprocessor;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.Template;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class StandardCompiler implements Compiler {

    private ClassResolver classResolver;

    public StandardCompiler (ClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Override
    public byte[] compile (String name, String source) {
        boolean printPreprocessedSource = false; // TODO: Add as configuration option.

        long prepStart = System.nanoTime ();

        StringWriter prepWriter = new StringWriter ();
        Preprocessor preprocessor = new Preprocessor (source, prepWriter, printPreprocessedSource);
        try {
            preprocessor.process ();
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle.
        }

        double prepTime = (System.nanoTime () - prepStart) / 1.0e9;

        if (printPreprocessedSource) {
            System.out.println (prepWriter.toString ());
        }

        long parseStart = System.nanoTime ();

        boolean usedSLL = false;
        BrygParser.StartContext startContext = null;
        InputStream stream = new ByteArrayInputStream (prepWriter.toString ().getBytes ());
        try {
            BrygLexer lexer = new BrygLexer (new ANTLRInputStream (stream));
            CommonTokenStream tokenStream = new CommonTokenStream (lexer);

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
                    parser.getInterpreter ().setPredictionMode (PredictionMode.LL_EXACT_AMBIG_DETECTION);
                    startContext = parser.start ();
                }
            }
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle.
        }

        if (startContext == null) return null;

        // TODO: Add printParseTree as configuration option.
        boolean printParseTree = false;
        if (printParseTree) {
            DebugVisitor debugVisitor = new DebugVisitor ();
            debugVisitor.visit (startContext);
        }

        double parseTime = (System.nanoTime () - parseStart) / 1.0e9;

        long jitStart = System.nanoTime ();

        ClassWriter classWriter = new ClassWriter (ClassWriter.COMPUTE_FRAMES);
        boolean printGeneratedBytecode = false; // TODO: Add as configuration option.
        ClassVisitor parentVisitor;
        if (printGeneratedBytecode) {
            parentVisitor = new TraceClassVisitor (classWriter, new PrintWriter (System.out));
        }else {
            parentVisitor = classWriter;
        }
        BrygClassVisitor brygClassVisitor = new BrygClassVisitor (parentVisitor);
        compile (brygClassVisitor, name, startContext, preprocessor.getLineToSourceLineMap ());

        double jitTime = (System.nanoTime () - jitStart) / 1.0e9;

        System.out.println ("Preprocessing took " + prepTime + "s.");
        System.out.println ("Parsing with " + (usedSLL ? "SLL(*)" : "LL(*)") +
                " took " + parseTime + "s.");
        System.out.println ("The JIT took " + jitTime + "s.");

        return classWriter.toByteArray ();
    }

    private void compile (ClassVisitor classVisitor, String name, BrygParser.StartContext startContext,
                          Map<Integer, Integer> lineToSourceLineMap) {
        classVisitor.visit (V1_7, ACC_PUBLIC, name.replace ('.', '/'), null, AsmTypes.getAsmType (Object.class).getInternalName (),
                new String[] { AsmTypes.getAsmType (Template.class).getInternalName () });
        {
            createEmptyConstructor (classVisitor);

            BrygMethodVisitor render = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC, "render",
                    TypeHelper.generateMethodDesc (
                            new Class<?>[] { Writer.class, Model.class },
                            Void.TYPE
                    ),
                    null,
                    new String[] { AsmTypes.getAsmType (InvalidInputParameterException.class).getInternalName () });
            {
                StandardVisitor visitor = new StandardVisitor (render, new BasicLibrary (), classResolver, lineToSourceLineMap);
                Node node = visitor.visit (startContext);

                boolean printAst = false; // TODO: Add as configuration option.
                if (printAst) {
                    node.print (System.out, 0);
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

    private void createEmptyConstructor (ClassVisitor classVisitor) {
        MethodVisitor constructor = classVisitor.visitMethod (ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitVarInsn (ALOAD, 0);
        constructor.visitMethodInsn (INVOKESPECIAL, AsmTypes.getAsmType (Object.class).getInternalName (), "<init>", "()V", false);
        constructor.visitInsn (RETURN);
        constructor.visitMaxs (1, 1);
        constructor.visitEnd ();
    }

}
