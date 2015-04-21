package io.collap.bryg.internal.compiler.ast.expression;

import io.collap.bryg.*;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.ClosureCompiler;
import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.FunctionUtil;
import io.collap.bryg.internal.compiler.util.ObjectCompileHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;

public class ClosureInstantiationExpression extends Expression {

    private ClosureType closureType;
    private ClosureScope closureScope;

    public ClosureInstantiationExpression(CompilationContext compilationContext, BrygParser.ClosureContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        String className = compilationContext.getUniqueClosureName();
        try {
            StandardEnvironment environment = compilationContext.getEnvironment();

            closureType = new ClosureType(compilationContext.getUnitType().getParentTemplateType(), className, ctx);
            List<ParameterInfo> parameterList = FunctionUtil.parseParameterList(environment,
                    ctx.parameterList().parameterDeclaration());
            FragmentInfo defaultFragment = new FragmentInfo(closureType, UnitType.DEFAULT_FRAGMENT_NAME, parameterList);
            closureType.addFragment(defaultFragment);
            setType(closureType);

            closureScope = new ClosureScope(compilationContext.getCurrentScope(), closureType);
            ClosureCompiler compiler = new ClosureCompiler(environment, closureType, closureScope);

            StandardClassLoader classLoader = environment.getStandardClassLoader();
            classLoader.addCompiler(compiler);
            Class<? extends Closure> closureClass = (Class<? extends Closure>) classLoader.loadClass(className);
            closureType.setClosureClass(closureClass);
        } catch (ClassNotFoundException e) {
            throw new BrygJitException("Could not compile closure instantiation!", getLine(), e);
        }
    }

    /**
     * This method does not compile the closure itself, but the <b>closure call</b>.
     */
    @Override
    public void compile() {
        BrygMethodVisitor mv = compilationContext.getMethodVisitor();
        List<Node> arguments = new ArrayList<>();

        /* The Environment is the first argument to the closure type. */
        arguments.add(new Node(compilationContext, getLine()) {
            @Override
            public void compile() {
                BrygMethodVisitor mv = compilationContext.getMethodVisitor();

                compilationContext.getFragmentScope()
                        .getThisVariable().compile(compilationContext, VariableUsageInfo.withGetMode());
                // -> StandardUnit

                mv.visitFieldInsn(GETFIELD, Types.fromClass(StandardUnit.class).getInternalName(),
                        "environment", Types.fromClass(Environment.class).getDescriptor());
                // StandardUnit -> Environment
            }
        });

        /* Load "this" as "__parent" parameter. */
        arguments.add(new VariableExpression(compilationContext, getLine(),
                compilationContext.getFragmentScope().getThisVariable(), VariableUsageInfo.withGetMode()));

        /* "Load" captured variables. */
        for (CompiledVariable capturedVariable : closureScope.getCapturedVariables()) {
            arguments.add(new VariableExpression(compilationContext, getLine(),
                    capturedVariable, VariableUsageInfo.withGetMode()));
        }

        new ObjectCompileHelper(mv, closureType).compileNew(closureType.getConstructorInfo().getDesc(), arguments);
        // -> Closure
    }

}
