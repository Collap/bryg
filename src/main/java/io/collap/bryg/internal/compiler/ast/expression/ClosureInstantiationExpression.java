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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;

public class ClosureInstantiationExpression extends Expression {

    private ClosureType closureType;
    private ClosureScope closureScope;

    public ClosureInstantiationExpression(CompilationContext compilationContext, BrygParser.ClosureContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        String className = compilationContext.getUniqueClosureName();
        try {
            StandardEnvironment environment = compilationContext.getEnvironment();

            @Nullable BrygParser.ParameterListContext parameterListContext = ctx.parameterList();
            List<ParameterInfo> parameterList = FunctionUtil.parseParameterList(environment,
                    parameterListContext != null ? parameterListContext.parameterDeclaration() : null);
            ClosureInterfaceType closureInterface = environment.getOrCreateClosureInterface(
                    parameterList.stream().map(VariableInfo::getType).collect(Collectors.toList())
            );

            closureType = new ClosureType(compilationContext.getUnitType().getParentTemplateType(),
                    closureInterface, className, ctx.closureBody());
            FragmentInfo defaultFragment = new FragmentInfo(closureType, UnitType.DEFAULT_FRAGMENT_NAME, true, parameterList);
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

                compilationContext.getFunctionScope()
                        .getThisVariable().compile(compilationContext, VariableUsageInfo.withGetMode());
                // -> StandardUnit

                mv.visitFieldInsn(
                        GETFIELD, Types.fromClass(StandardUnit.class).getInternalName(),
                        StandardUnit.ENVIRONMENT_FIELD_NAME,
                        Types.fromClass(StandardEnvironment.class).getDescriptor()
                );
                // StandardUnit -> StandardEnvironment
            }
        });

        /* Load "this" as "__parent" parameter. */
        arguments.add(new VariableExpression(compilationContext, getLine(),
                compilationContext.getFunctionScope().getThisVariable(), VariableUsageInfo.withGetMode()));

        /* "Load" captured variables. */
        for (CompiledVariable capturedVariable : closureScope.getCapturedVariables()) {
            arguments.add(new VariableExpression(compilationContext, getLine(),
                    capturedVariable, VariableUsageInfo.withGetMode()));
        }

        new ObjectCompileHelper(mv, closureType).compileNew(closureType.getConstructorInfo().getDesc(), arguments);
        // -> Closure
    }

}
