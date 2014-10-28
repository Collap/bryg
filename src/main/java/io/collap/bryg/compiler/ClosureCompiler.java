package io.collap.bryg.compiler;

import bryg.org.objectweb.asm.*;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.closure.Closure;
import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.compiler.ast.Node;
import io.collap.bryg.compiler.bytecode.BrygClassVisitor;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.ClosureScope;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.model.Model;
import io.collap.bryg.unit.StandardUnit;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;
import static bryg.org.objectweb.asm.Opcodes.RETURN;

public class ClosureCompiler implements Compiler<ClosureType> {

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
                new Type (StandardUnit.class).getAsmType ().getInternalName (),
                new String[] { new Type (Closure.class).getAsmType ().getInternalName () });
        {
            Context context = new Context (parentContext.getEnvironment (), closureType, null, closureType.getClosureScope ());
            Node node = context.getParseTreeVisitor ().visit (closureType.getClosureContext ());

            /* Make sure the node is created before these methods are called, so every captured variable
               is correctly turned into a field and constructor parameter. */
            createFields (classVisitor);
            createConstructor (classVisitor);

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

                /* Define local variables for the closure variable.
                   This is currently necessary, because we would have to extend
                   variable access to allow field access in a closure. */
                initializeLocalClosureVariables (context);

                node.compile ();

                mv.voidReturn ();
                mv.visitMaxs (0, 0); /* Note: This function is called so the maximum values are calculated by ASM.
                                                  The arguments 0 and 0 have no special meaning. */
            }
            mv.visitEnd ();
        }
        classVisitor.visitEnd ();
    }

    private void createFields (ClassVisitor classVisitor) {
        List<Variable> capturedVariables = closureType.getClosureScope ().getCapturedVariables ();
        for (Variable variable : capturedVariables) {
            FieldVisitor fieldVisitor = classVisitor.visitField (Opcodes.ACC_PRIVATE, variable.getName (), variable.getType ().getAsmType ().getDescriptor (),
                    null, null);
            fieldVisitor.visitEnd ();

            /* Create setter. */
            /* String setterName = IdUtil.createSetterName (variable.getName ());
            String desc = TypeHelper.generateMethodDesc (
                    new Type[] { variable.getType () },
                    new Type (Void.TYPE)
            );
            MethodVisitor methodVisitor = classVisitor.visitMethod (Opcodes.ACC_PUBLIC, setterName, desc, null, null);

            methodVisitor.visitInsn (Opcodes.RETURN);
            methodVisitor.visitMaxs (0, 0); */
        }
    }

    private void createConstructor (ClassVisitor classVisitor) {
        List<Variable> capturedVariables = closureType.getClosureScope ().getCapturedVariables ();
        int parameterCount = capturedVariables.size () + 1;

        List<Class<?>> parameterTypes = new ArrayList<> (parameterCount);
        parameterTypes.add (Environment.class); /* This is the only parameter for the StandardUnit. */
        for (Variable variable : capturedVariables) {
            parameterTypes.add (variable.getType ().getJavaType ());
        }

        String desc = TypeHelper.generateMethodDesc (parameterTypes.toArray (new Class<?>[parameterCount]), Void.TYPE);
        closureType.setConstructorDesc (desc);
        MethodVisitor constructor = classVisitor.visitMethod (ACC_PUBLIC, "<init>", desc, null, null);

        /* Call super. */
        constructor.visitVarInsn (ALOAD, 0); /* this */
        constructor.visitVarInsn (ALOAD, 1); /* environment */
        constructor.visitMethodInsn (INVOKESPECIAL,
                AsmTypes.getAsmType (StandardUnit.class).getInternalName (),
                "<init>", TypeHelper.generateMethodDesc (
                        new Class[] { Environment.class },
                        Void.TYPE
                ), false);

        /* Set fields. */
        for (int i = 0; i < capturedVariables.size (); ++i) {
            Variable variable = capturedVariables.get (i);
            int id = i + 2;
            bryg.org.objectweb.asm.Type asmType = variable.getType ().getAsmType ();

            constructor.visitVarInsn (ALOAD, 0); /* this */
            constructor.visitVarInsn (asmType.getOpcode (ILOAD), id);
            constructor.visitFieldInsn (PUTFIELD, TypeHelper.toInternalName (closureType.getFullName ()),
                    variable.getName (), asmType.getDescriptor ());
        }

        constructor.visitInsn (RETURN);
        constructor.visitMaxs (2, 2);
        constructor.visitEnd ();
    }

    private void initializeLocalClosureVariables (Context context) {
        BrygMethodVisitor mv = context.getMethodVisitor ();
        ClosureScope closureScope = closureType.getClosureScope ();

        List<Variable> capturedVariables = closureScope.getCapturedVariables ();
        for (Variable capturedVariable : capturedVariables) {
            String name = capturedVariable.getName ();
            System.out.println ("Captured variable name: " + name);
            Variable closureVariable = closureScope.getVariable (name);
            bryg.org.objectweb.asm.Type asmType = closureVariable.getType ().getAsmType ();

            mv.visitVarInsn (ALOAD, 0); /* this */
            mv.visitFieldInsn (GETFIELD, TypeHelper.toInternalName (closureType.getFullName ()),
                name, asmType.getDescriptor ());
            mv.visitVarInsn (asmType.getOpcode (ISTORE), closureVariable.getId ());
        }
    }

    @Override
    public ClosureType getUnitType () {
        return closureType;
    }

}
