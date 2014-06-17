package io.collap.bryg.compiler;

import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.expression.ClassType;
import io.collap.bryg.compiler.expression.PrimitiveType;
import io.collap.bryg.compiler.expression.Type;
import io.collap.bryg.compiler.library.BasicLibrary;
import io.collap.bryg.compiler.parser.BrygClassVisitor;
import io.collap.bryg.compiler.parser.BrygMethodVisitor;
import io.collap.bryg.compiler.parser.DebugVisitor;
import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.util.TypeHelper;
import io.collap.bryg.model.Model;
import io.collap.bryg.parser.BrygLexer;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.compiler.preprocessor.Preprocessor;
import io.collap.bryg.template.InvalidInputParameterException;
import io.collap.bryg.template.Template;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

import static org.objectweb.asm.Opcodes.*;

public class TestCompiler implements Compiler {

    private ClassResolver classResolver;

    public TestCompiler (ClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Override
    public byte[] compile (String name, String source) {
        StringWriter prepWriter = new StringWriter ();
        Preprocessor preprocessor = new Preprocessor (source, prepWriter, true);
        try {
            preprocessor.process ();
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle.
        }

        System.out.println (prepWriter.toString ());

        BrygParser.StartContext startContext = null;
        InputStream stream = new ByteArrayInputStream (prepWriter.toString ().getBytes ());
        try {
            BrygLexer lexer = new BrygLexer (new ANTLRInputStream (stream));
            CommonTokenStream tokenStream = new CommonTokenStream (lexer);
            BrygParser parser = new BrygParser (tokenStream);
            startContext = parser.start ();
        } catch (IOException e) {
            e.printStackTrace (); // TODO: Handle.
        }

        if (startContext == null) return null;

        // TODO: Remove following debug.
        DebugVisitor debugVisitor = new DebugVisitor ();
        debugVisitor.visit (startContext);

        PrintWriter debugWriter = new PrintWriter (System.out);

        ClassWriter classWriter = new ClassWriter (ClassWriter.COMPUTE_MAXS);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor (classWriter, debugWriter);
        BrygClassVisitor brygClassVisitor = new BrygClassVisitor (traceClassVisitor);
        compile (brygClassVisitor, name, startContext);
        return classWriter.toByteArray ();
    }

    private void compile (ClassVisitor classVisitor, String name, BrygParser.StartContext startContext) {
        classVisitor.visit (V1_7, ACC_PUBLIC, name, null, ClassType.OBJECT.getJvmName (),
                new String[] { new ClassType (Template.class).getJvmName () });
        {
            createEmptyConstructor (classVisitor);

            BrygMethodVisitor render = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC, "render",
                    TypeHelper.generateMethodDesc (
                            new Type[] { new ClassType (Writer.class), new ClassType (Model.class) },
                            PrimitiveType._void
                    ),
                    null,
                    new String[] { InvalidInputParameterException.CLASS_TYPE.getJvmName () });
            {
                RenderVisitor visitor = new RenderVisitor (render, new BasicLibrary (), classResolver);
                Node node = visitor.visit (startContext);
                node.print (System.out, 0);
                node.compile ();

                render.voidReturn ();
                render.visitMaxs (2, 3);
            }
            render.visitEnd ();
        }
        classVisitor.visitEnd ();
    }

    private void createEmptyConstructor (ClassVisitor classVisitor) {
        MethodVisitor constructor = classVisitor.visitMethod (ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitVarInsn (ALOAD, 0);
        constructor.visitMethodInsn (INVOKESPECIAL, ClassType.OBJECT.getJvmName (), "<init>", "()V", false);
        constructor.visitInsn (RETURN);
        constructor.visitMaxs (1, 1);
        constructor.visitEnd ();
    }

}
