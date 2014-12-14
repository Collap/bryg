package io.collap.bryg.compiler;

import bryg.org.objectweb.asm.*;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.closure.StandardClosure;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.bytecode.BrygClassVisitor;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.compiler.type.*;
import io.collap.bryg.model.Model;
import io.collap.bryg.unit.StandardUnit;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public class ClosureCompiler extends UnitCompiler implements Compiler<ClosureType> {

    private Context parentContext;
    private ClosureType closureType;

    public ClosureCompiler (Context parentContext, ClosureType closureType) {
        this.parentContext = parentContext;
        this.closureType = closureType;
    }

    public byte[] compile () {
        long jitStart = System.nanoTime ();

        ClassWriter classWriter = new ClassWriter (ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parentVisitor;
        // if (.shouldPrintBytecode ()) {
            parentVisitor = new TraceClassVisitor (classWriter, new PrintWriter (System.out));
        // }else {
            //parentVisitor = classWriter;
        // }

        BrygClassVisitor brygClassVisitor = new BrygClassVisitor (parentVisitor);
        compile (brygClassVisitor);
        /* if (configuration.shouldPrintBytecode ()) {
            System.out.println ();
        } */

        double jitTime = (System.nanoTime () - jitStart) / 1.0e9;

        System.out.println ("Jitting the block took " + jitTime + "s.");

        return classWriter.toByteArray ();
    }

    private void compile (ClassVisitor classVisitor) {
        classVisitor.visit (
                V1_7, ACC_PUBLIC, TypeHelper.toInternalName (closureType.getFullName ()), null,
                Types.fromClass (StandardUnit.class).getInternalName (),
                new String[] { Types.fromClass (Closure.class).getInternalName () });
        {
            classVisitor.visitSource (closureType.getParentTemplateType ().getSimpleName () + ".bryg", null);

            Context context = new Context (parentContext.getEnvironment (), closureType.getFragment ("render"),
                closureType, null, closureType.getClosureScope ());
            Node node = context.getParseTreeVisitor ().visit (closureType.getClosureContext ());

            List<VariableInfo> fields = new ArrayList<> ();
            fields.add (new VariableInfo (closureType, StandardClosure.PARENT_FIELD_NAME, false, false));
            List<Variable> capturedVariables = closureType.getClosureScope ().getCapturedVariables ();
            for (Variable variable : capturedVariables) {
                fields.add (variable.getInfo ());
            }

            /* Make sure the node is created before these methods are called, so every captured variable
               is correctly turned into a field and constructor parameter. */
            compileFields (classVisitor, fields);
            compileConstructor (classVisitor, closureType, true, fields);

            BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod (ACC_PUBLIC, "render",
                    TypeHelper.generateMethodDesc (
                            new Class<?>[] { Writer.class, Model.class },
                            Void.TYPE
                    ), null, null);
            {
                context.setMethodVisitor (mv);

                /* if (configuration.shouldPrintAst ()) {
                    node.print (System.out, 0);
                    System.out.println ();
                } */

                node.compile ();

                mv.voidReturn ();
                mv.visitMaxsAuto ();
            }
            mv.visitEnd ();
        }
        classVisitor.visitEnd ();
    }

    @Override
    public ClosureType getUnitType () {
        return closureType;
    }

}
