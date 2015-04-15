package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.*;
import io.collap.bryg.internal.*;
import io.collap.bryg.internal.compiler.ClosureCompiler;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.compiler.util.ObjectCompileHelper;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;

public class ClosureDeclarationNode extends Node {

    private ClosureType closureType;
    private ClosureScope closureScope;

    public ClosureDeclarationNode(CompilationContext compilationContext, BrygParser.ClosureContext ctx) {
        super(compilationContext, ctx.getStart().getLine());

        ClosureScope closureScope = new ClosureScope(compilationContext.getCurrentScope());


        // TODO: Register the following variable with the fragment scope. (Do we need to?)
        registerLocalVariable(new LocalVariable(Types.fromClass(Model.class), "model",
                Mutability.immutable, Nullness.notnull));


        String className = compilationContext.getUniqueClosureName();
        try {
            closureType = new ClosureType(compilationContext.getUnitType().getParentTemplateType(), className, closureScope, ctx);
            closureType.addFragment(new ClosureFragmentInfo("render"));
            ClosureCompiler compiler = new ClosureCompiler(compilationContext, closureType);
            ClosureClassLoader closureClassLoader = new ClosureClassLoader(compilationContext.getEnvironment(), compiler);
            Class<? extends Closure> closureClass = (Class<? extends Closure>) closureClassLoader.loadClass(className);
            compilationContext.getEnvironment().getClassCache().cacheClass(className, closureClass);
            closureType.setClosureClass(closureClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // TODO: Handle
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
        arguments.add(new Node(compilationContext) {
            @Override
            public void compile() {
                BrygMethodVisitor mv = compilationContext.getMethodVisitor();

                new VariableExpression(compilationContext, getLine(),
                        compilationContext.getFragmentScope().getThisVariable(),
                        AccessMode.get).compile();
                // -> StandardUnit

                mv.visitFieldInsn(GETFIELD, Types.fromClass(StandardUnit.class).getInternalName(),
                        "environment", Types.fromClass(Environment.class).getDescriptor());
                // StandardUnit -> Environment
            }
        });

        /* Load "this" as "__parent" parameter. */
        arguments.add(new VariableExpression(compilationContext, getLine(),
                compilationContext.getFragmentScope().getThisVariable(), AccessMode.get));

        /* "Load" captured variables. */
        for (CompiledVariable capturedVariable : closureType.getClosureScope().getCapturedVariables()) {
            arguments.add(new VariableExpression(compilationContext, getLine(), capturedVariable, AccessMode.get));
        }

        new ObjectCompileHelper(mv, closureType).compileNew(closureType.getConstructorDesc(), arguments);
        // -> Closure
    }

    public ClosureType getClosureType() {
        return closureType;
    }

}
