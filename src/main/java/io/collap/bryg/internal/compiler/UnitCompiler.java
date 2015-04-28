package io.collap.bryg.internal.compiler;

import bryg.org.objectweb.asm.*;
import bryg.org.objectweb.asm.util.TraceClassVisitor;
import io.collap.bryg.*;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.Type;
import io.collap.bryg.internal.compiler.ast.DelegatorBodyNode;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.ast.FunctionBodyNode;
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

            FunctionBodyNode node = new FunctionBodyNode(compilationContext, compileInfo.getStatementContexts());

            if (environment.getDebugConfiguration().shouldPrintAst()) {
                node.print(System.out, 0);
                System.out.println();
            }

            node.compile();

            mv.voidReturn();
            mv.visitMaxsAuto();
        }
        mv.visitEnd();
    }

    protected void compileFragmentDelegator(ClassVisitor classVisitor, String delegatorName,
                                            FragmentInfo fragmentInfo, UnitScope unitScope) {
        final BrygMethodVisitor mv = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC,
                delegatorName,
                TypeHelper.generateMethodDesc(
                        new Class<?>[]{Writer.class, Model.class},
                        Void.TYPE
                ),
                null,
                new String[]{AsmTypes.getAsmType(InvalidInputParameterException.class).getInternalName()});
        {
            List<ParameterInfo> delegatorParameters = new ArrayList<>();
            delegatorParameters.add(new ParameterInfo(Types.fromClass(Writer.class), "writer", Mutability.immutable,
                    Nullness.notnull, null));
            delegatorParameters.add(new ParameterInfo(Types.fromClass(Model.class), "model", Mutability.immutable,
                    Nullness.notnull, null));
            FragmentScope fragmentScope = new FragmentScope(unitScope, unitType, delegatorParameters);
            CompilationContext compilationContext = new CompilationContext(environment, fragmentInfo, unitType,
                    mv, fragmentScope, unitScope);
            CompiledVariable thisVariable = fragmentScope.getThisVariable();
            CompiledVariable writerVariable = fragmentScope.getWriterVariable();
            CompiledVariable modelVariable = fragmentScope.getMandatoryVariable("model");

            DelegatorBodyNode root = new DelegatorBodyNode(compilationContext);
            root.addChild(new VariableExpression(compilationContext, Node.UNKNOWN_LINE, thisVariable, VariableUsageInfo.withGetMode()));
            // -> unitType

            /* Add all parameters in order. */
            root.addChild(new VariableExpression(compilationContext, Node.UNKNOWN_LINE, writerVariable, VariableUsageInfo.withGetMode()));
            // -> Writer

            // i starts at 1, because we skip the already loaded Writer parameter.
            List<ParameterInfo> directParameters = fragmentInfo.getParameters();
            for (int i = 1; i < directParameters.size(); i++) {
                ParameterInfo parameter = directParameters.get(i);
                root.addChild(new ModelLoadExpression(compilationContext, parameter, modelVariable));
                // -> T
            }

            /* Call the actual fragment function. */
            root.addChild(new Node(compilationContext, Node.UNKNOWN_LINE) {
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
     *
     * // TODO: Set constructor info in unit type.
     */
    protected void compileConstructor(ClassVisitor classVisitor, UnitType unitType) {
        ConstructorInfo constructorInfo = unitType.getConstructorInfo();

        BrygMethodVisitor constructor = (BrygMethodVisitor) classVisitor.visitMethod(ACC_PUBLIC, "<init>",
                constructorInfo.getDesc(), null, null);
        compileSuperConstructorCall(constructor);

        // Set fields.
        // params - fields + "this" = id of first field
        List<FieldInfo> fields = unitType.getFields();
        for (int i = 0, id = constructorInfo.getParameters().size() - fields.size() + 1; i < fields.size(); ++i) {
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
        delegatorParameters.add(new ParameterInfo(Types.fromClass(StandardEnvironment.class),
                StandardUnit.ENVIRONMENT_FIELD_NAME, Mutability.immutable, Nullness.notnull, null));
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

        DelegatorBodyNode root = new DelegatorBodyNode(compilationContext);

        // Load and store.
        List<FieldInfo> fields = unitType.getFields();
        for (FieldInfo field : fields) {
            @Nullable CompiledVariable instanceVariable = unitScope.getVariable(field.getName());
            if (instanceVariable == null) {
                throw new CompilationException("The field " + field.getName() + " does not have a corresponding" +
                        " variable in the unit scope, which should NOT happen and is definitely a compiler bug.");
            }

            // Also forces the assignment, because the fields are immutable.
            root.addChild(new VariableExpression(compilationContext, Node.UNKNOWN_LINE, instanceVariable,
                    VariableUsageInfo.withSetMode(new ModelLoadExpression(compilationContext, field,
                            constructorScope.getMandatoryVariable("model"))), true));
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
