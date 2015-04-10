package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.*;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.DelegatorRootNode;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.RootNode;
import io.collap.bryg.internal.compiler.ast.expression.ModelLoadExpression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.type.AsmTypes;
import io.collap.bryg.internal.type.TypeHelper;
import io.collap.bryg.internal.type.Types;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.*;

public abstract class UnitCompiler<T extends UnitType> implements Compiler<T> {

    // TODO: Implement delegators for all units, not just templates?

    // TODO: Note that due to the way constructors are created, you may not have a template with just a Model as a parameter.
    //       Additionally, restrict this circumstance by actually checking for it in the UnitCompiler.

    protected StandardEnvironment environment;
    protected T unitType;

    public UnitCompiler(StandardEnvironment environment, T unitType) {
        this.environment = environment;
        this.unitType = unitType;
    }

    @Override
    public byte[] compile() {
        long jitStart = System.nanoTime();

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor parentVisitor;
        if (environment.getDebugConfiguration().shouldPrintBytecode()) {
            parentVisitor = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        } else {
            parentVisitor = classWriter;
        }
        BrygClassVisitor brygClassVisitor = new BrygClassVisitor(parentVisitor);
        compileClass(brygClassVisitor);
        if (environment.getDebugConfiguration().shouldPrintBytecode()) {
            System.out.println();
        }

        double jitTime = (System.nanoTime() - jitStart) / 1.0e9;

        System.out.println("The JIT took " + jitTime + "s.");

        return classWriter.toByteArray();
    }

    /**
     * TODO: Comment.
     */
    protected abstract void compileClass(ClassVisitor classVisitor);

    protected void compileFragment(ClassVisitor classVisitor, FragmentInfo fragmentInfo,
                                   FragmentCompileInfo compileInfo, UnitScope unitScope) {
        BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC,
                fragmentInfo.getDirectName(), fragmentInfo.getDesc(), null, null);
        {
            FunctionScope fragmentScope = new FragmentScope(unitScope, unitType, fragmentInfo.getParameters());
            CompilationContext compilationContext = new CompilationContext(environment, fragmentInfo, unitType, mv,
                    fragmentScope, unitScope);

            RootNode node = new RootNode(compilationContext, compileInfo.getStatementContexts());

            if (environment.getDebugConfiguration().shouldPrintAst()) {
                node.print(System.out, 0);
                System.out.println();
            }

            node.addGlobalVariableLoads(fragmentScope);
            node.compile();

            mv.voidReturn();
            mv.visitMaxsAuto();
        }
        mv.visitEnd();
    }

    protected void compileFragmentDelegator(ClassVisitor classVisitor, FragmentInfo fragmentInfo, UnitScope unitScope) {
        final BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC,
                fragmentInfo.getName(),
                TypeHelper.generateMethodDesc(
                        new Class<?>[]{Writer.class, Model.class},
                        Void.TYPE
                ),
                null,
                new String[]{AsmTypes.getAsmType(InvalidInputParameterException.class).getInternalName()});
        {
            List<ParameterInfo> delegatorParameters = new ArrayList<>();
            delegatorParameters.add(new ParameterInfo(Types.fromClass(Model.class), "model", Mutability.immutable,
                    Nullness.notnull, null));
            FragmentScope fragmentScope = new FragmentScope(unitScope, unitType, delegatorParameters);
            CompilationContext compilationContext = new CompilationContext(environment, fragmentInfo, unitType,
                    mv, fragmentScope, unitScope);
            CompiledVariable thisVariable = fragmentScope.getThisVariable();
            CompiledVariable writerVariable = fragmentScope.getWriterVariable();
            CompiledVariable modelVariable = fragmentScope.getMandatoryVariable("model");

            DelegatorRootNode root = new DelegatorRootNode(compilationContext);
            root.addChild(new VariableExpression(compilationContext, -1, thisVariable, VariableUsageInfo.withGetMode()));
            // -> unitType

            /* Add all parameters in order. */
            root.addChild(new VariableExpression(compilationContext, -1, writerVariable, VariableUsageInfo.withGetMode()));
            // -> Writer

            List<ParameterInfo> directParameters = fragmentInfo.getParameters();
            for (ParameterInfo parameter : directParameters) {
                root.addChild(new ModelLoadExpression(compilationContext, parameter, modelVariable));
                // -> T
            }

            /* Call the actual fragment function. */
            root.addChild(new Node(compilationContext) {
                @Override
                public void compile() {
                    mv.visitMethodInsn(INVOKEVIRTUAL, fragmentInfo.getOwner().getInternalName(),
                            fragmentInfo.getDirectName(), fragmentInfo.getDesc(), false);
                }
            });
            // unitType, Writer, P1, P2, P3, ... ->

            if (environment.getDebugConfiguration().shouldPrintAst()) {
                root.print(System.out, 0);
                System.out.println();
            }

            root.compile();

            mv.voidReturn();
            mv.visitMaxsAuto();
        }
        mv.visitEnd();
    }

    /**
     * The only implicit parameter is the environment.
     * Also sets the constructorDesc field of the unitType.
     */
    protected void compileConstructor(ClassVisitor classVisitor, UnitType unitType, boolean shouldSetConstructorDesc) {
        List<Type> parameterTypes = new ArrayList<>();
        List<FieldInfo> fields = unitType.getFields();
        parameterTypes.add(Types.fromClass(StandardEnvironment.class)); /* This is the only parameter for the StandardUnit. */
        for (FieldInfo field : fields) {
            parameterTypes.add(field.getType());
        }

        String desc = TypeHelper.generateMethodDesc(
                parameterTypes.toArray(new Type[parameterTypes.size()]),
                Types.fromClass(Void.TYPE)
        );
        if (shouldSetConstructorDesc) {
            unitType.setConstructorDesc(desc);
        }

        BrygMethodVisitor constructor = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC, "<init>", desc, null, null);
        compileSuperConstructorCall(constructor);

        // Set fields.
        for (int i = 0, id = parameterTypes.size() - fields.size() + 1; // params - fields + "this" = id of first field
             i < fields.size(); ++i) {
            FieldInfo field = fields.get(i);
            Type fieldType = field.getType();

            constructor.visitVarInsn(ALOAD, 0); /* this */
            constructor.visitVarInsn(fieldType.getOpcode(ILOAD), id);
            constructor.visitFieldInsn(PUTFIELD, unitType.getInternalName(),
                    field.getName(), fieldType.getDescriptor());

            id += field.getType().getStackSize();
        }

        compileConstructorEnd(constructor);
    }

    /**
     * Parameters:
     *      StandardEnvironment
     *      Model
     */
    protected void compileConstructorDelegator(ClassVisitor classVisitor, UnitScope unitScope) {
        String desc = TypeHelper.generateMethodDesc(
                new Type[]{Types.fromClass(StandardEnvironment.class), Types.fromClass(Model.class)},
                Types.fromClass(Void.TYPE)
        );

        BrygMethodVisitor constructor = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC, "<init>", desc, null, null);
        compileSuperConstructorCall(constructor);

        /* Load values from model and store them. */
        ParameterInfo modelParameter = new ParameterInfo(Types.fromClass(Model.class), "model",
                Mutability.immutable, Nullness.notnull, null);
        List<ParameterInfo> delegatorParameters = new ArrayList<>();
        delegatorParameters.add(modelParameter);
        final ConstructorScope constructorScope = new ConstructorScope(unitScope, unitType, delegatorParameters);

        List<ParameterInfo> parameters = new ArrayList<>();
        parameters.add(new ParameterInfo(constructorScope.getEnvironmentVariable(), null));
        parameters.add(modelParameter);

        CompilationContext compilationContext = new CompilationContext(
                environment,
                new ConstructorInfo(
                        unitType,
                        "<init>",
                        parameters
                ),
                unitType,
                constructor,
                constructorScope,
                unitScope
        );

        DelegatorRootNode root = new DelegatorRootNode(compilationContext);

        /* Load and store. */
        List<FieldInfo> fields = unitType.getFields();
        for (FieldInfo field : fields) {
            @Nullable CompiledVariable instanceVariable = unitScope.getVariable(field.getName());
            if (instanceVariable == null) {
                throw new CompilationException("The field " + field.getName() + " does not have a corresponding" +
                        " variable in the unit scope, which should NOT happen and is definitely a compiler bug.");
            }

            root.addChild(new VariableExpression(compilationContext, -1, instanceVariable,
                    VariableUsageInfo.withSetMode(new ModelLoadExpression(compilationContext, field,
                            constructorScope.getMandatoryVariable("model")))));
        }

        root.compile();

        compileConstructorEnd(constructor);
    }

    /**
     * Calls the constructor of {@link io.collap.bryg.internal.StandardUnit}.
     */
    private void compileSuperConstructorCall(BrygMethodVisitor constructor) {
        constructor.visitVarInsn(ALOAD, 0); /* this */
        constructor.visitVarInsn(ALOAD, 1); /* environment */
        constructor.visitMethodInsn(INVOKESPECIAL,
                Types.fromClass(unitType.getStandardUnitClass()).getInternalName(),
                "<init>", TypeHelper.generateMethodDesc(
                        new Class[]{StandardEnvironment.class},
                        Void.TYPE
                ), false);
    }

    private void compileConstructorEnd(BrygMethodVisitor constructor) {
        constructor.voidReturn();
        constructor.visitMaxsAuto();
        constructor.visitEnd();
    }

    protected void compileFields(ClassVisitor classVisitor, List<FieldInfo> fields) {
        for (FieldInfo field : fields) {
            FieldVisitor fieldVisitor = classVisitor.visitField(Opcodes.ACC_PRIVATE, field.getName(),
                    field.getType().getDescriptor(), null, null);
            fieldVisitor.visitEnd();
        }
    }

    @Override
    public T getUnitType() {
        return unitType;
    }

}
