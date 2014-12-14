package io.collap.bryg.compiler;

import bryg.org.objectweb.asm.ClassVisitor;
import bryg.org.objectweb.asm.FieldVisitor;
import bryg.org.objectweb.asm.MethodVisitor;
import bryg.org.objectweb.asm.Opcodes;
import io.collap.bryg.compiler.scope.VariableInfo;
import io.collap.bryg.compiler.type.AsmTypes;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.compiler.type.TypeHelper;
import io.collap.bryg.compiler.type.Types;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.unit.UnitType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public abstract class UnitCompiler {

    /**
     * The only implicit parameter is the environment.
     * Also sets the constructorDesc field of the unitType.
     */
    protected void compileConstructor (ClassVisitor classVisitor,
                                       UnitType unitType, boolean shouldSetConstructorDesc,
                                       @Nullable List<VariableInfo> parameters) {
        List<Type> parameterTypes = new ArrayList<> ();
        parameterTypes.add (Types.fromClass (Environment.class)); /* This is the only parameter for the StandardUnit. */
        if (parameters != null) {
            for (VariableInfo parameter : parameters) {
                parameterTypes.add (parameter.getType ());
            }
        }

        String desc = TypeHelper.generateMethodDesc (parameterTypes.toArray (new Type[parameterTypes.size ()]),
                Types.fromClass (Void.TYPE));
        if (shouldSetConstructorDesc) {
            unitType.setConstructorDesc (desc);
        }
        MethodVisitor constructor = classVisitor.visitMethod (ACC_PUBLIC, "<init>", desc, null, null);

        /* Call super. */
        constructor.visitVarInsn (ALOAD, 0); /* this */
        constructor.visitVarInsn (ALOAD, 1); /* environment */
        constructor.visitMethodInsn (INVOKESPECIAL,
                AsmTypes.getAsmType (unitType.getStandardUnitClass ()).getInternalName (),
                "<init>", TypeHelper.generateMethodDesc (
                        new Class[] { Environment.class },
                        Void.TYPE
                ), false);

        /* Set fields. */
        if (parameters != null) {
            for (int i = 0, id = 2; i < parameters.size (); ++i) {
                VariableInfo parameter = parameters.get (i);
                Type parameterType = parameter.getType ();

                constructor.visitVarInsn (ALOAD, 0); /* this */
                constructor.visitVarInsn (parameterType.getOpcode (ILOAD), id);
                constructor.visitFieldInsn (PUTFIELD, unitType.getInternalName (),
                        parameter.getName (), parameterType.getDescriptor ());

                id += parameter.getType ().getStackSize ();
            }
        }

        constructor.visitInsn (RETURN);
        constructor.visitMaxs (0, 0);
        constructor.visitEnd ();
    }

    protected void compileFields (ClassVisitor classVisitor, List<VariableInfo> parameters) {
        for (VariableInfo parameter : parameters) {
            FieldVisitor fieldVisitor = classVisitor.visitField (Opcodes.ACC_PRIVATE, parameter.getName (),
                    parameter.getType ().getDescriptor (), null, null);
            fieldVisitor.visitEnd ();
        }
    }

}
