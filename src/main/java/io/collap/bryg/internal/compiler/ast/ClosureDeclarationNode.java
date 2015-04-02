package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.Closure;
import io.collap.bryg.closure.ClosureClassLoader;
import io.collap.bryg.closure.ClosureFragmentInfo;
import io.collap.bryg.internal.ClosureType;
import io.collap.bryg.internal.compiler.ClosureCompiler;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.BrygMethodVisitor;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.compiler.util.ObjectCompileHelper;
import io.collap.bryg.internal.scope.ClosureScope;
import io.collap.bryg.internal.scope.UnitScope;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.internal.type.Types;
import io.collap.bryg.Environment;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.internal.StandardUnit;

import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.GETFIELD;

public class ClosureDeclarationNode extends Node {

    private ClosureType closureType;

    public ClosureDeclarationNode (Context context, BrygParser.ClosureContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        ClosureScope closureScope = new ClosureScope (new UnitScope (null), closureType, context.getCurrentScope ());

        String className = context.getUniqueClosureName ();
        try {
            closureType = new ClosureType (context.getUnitType ().getParentTemplateType (), className, closureScope, ctx);
            closureType.addFragment (new ClosureFragmentInfo ("render"));
            ClosureCompiler compiler = new ClosureCompiler (context, closureType);
            ClosureClassLoader closureClassLoader = new ClosureClassLoader (context.getEnvironment (), compiler);
            Class<? extends Closure> closureClass = (Class<? extends Closure>) closureClassLoader.loadClass (className);
            context.getEnvironment ().getClassCache ().cacheClass (className, closureClass);
            closureType.setClosureClass (closureClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }
    }

    @Override
    public void compile () {
        BrygMethodVisitor mv = context.getMethodVisitor ();

        List<Node> arguments = new ArrayList<> ();
        /* The Environment is the first argument to the closure type. */
        arguments.add (new Node (context) {
            @Override
            public void compile () {
                BrygMethodVisitor mv = context.getMethodVisitor ();

                new VariableExpression (context, getLine (),
                        context.getHighestLocalScope().getVariable ("this"), AccessMode.get).compile ();
                // -> StandardUnit

                mv.visitFieldInsn (GETFIELD, Types.fromClass (StandardUnit.class).getInternalName (),
                        "environment", Types.fromClass (Environment.class).getDescriptor ());
                // StandardUnit -> Environment
            }
        });

        /* Load "this" as "__parent" parameter. */
        arguments.add (new VariableExpression (context, getLine (),
                context.getHighestLocalScope().getVariable ("this"), AccessMode.get));

        /* Load "model" as "__parent_model" parameter. */
        arguments.add (new VariableExpression (context, getLine (),
                context.getHighestLocalScope().getVariable ("model"), AccessMode.get));

        /* "Load" captured variables. */
        for (Variable capturedVariable : closureType.getClosureScope ().getCapturedVariables ()) {
            arguments.add (new VariableExpression (context, getLine (), capturedVariable, AccessMode.get));
        }

        new ObjectCompileHelper (mv, closureType).compileNew (closureType.getConstructorDesc (), arguments);
        // -> Closure
    }

    public ClosureType getClosureType () {
        return closureType;
    }

}