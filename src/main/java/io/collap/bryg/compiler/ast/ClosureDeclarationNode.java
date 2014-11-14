package io.collap.bryg.compiler.ast;

import io.collap.bryg.closure.Closure;
import io.collap.bryg.closure.ClosureClassLoader;
import io.collap.bryg.closure.ClosureFragmentInfo;
import io.collap.bryg.closure.ClosureType;
import io.collap.bryg.compiler.ClosureCompiler;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.helper.ObjectCompileHelper;
import io.collap.bryg.compiler.scope.ClosureScope;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.compiler.type.Type;
import io.collap.bryg.environment.Environment;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.StandardUnit;

import java.util.ArrayList;
import java.util.List;

import static bryg.org.objectweb.asm.Opcodes.ALOAD;
import static bryg.org.objectweb.asm.Opcodes.GETFIELD;

public class ClosureDeclarationNode extends Node {

    private ClosureType closureType;

    public ClosureDeclarationNode (Context context, BrygParser.ClosureContext ctx) {
        super (context);
        setLine (ctx.getStart ().getLine ());

        ClosureScope closureScope = new ClosureScope (context.getCurrentScope ());

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

                mv.visitVarInsn (ALOAD, context.getRootScope ().getVariable ("this").getId ());
                // -> StandardUnit

                mv.visitFieldInsn (GETFIELD, new Type (StandardUnit.class).getAsmType ().getInternalName (),
                        "environment", new Type (Environment.class).getAsmType ().getDescriptor ());
                // StandardUnit -> Environment
            }
        });

        /* Load "this" as "__parent" parameter. */
        arguments.add (new VariableExpression (context, context.getRootScope ().getVariable ("this"),
                AccessMode.get, getLine ()));

        /* Load "model" as "__parent_model" parameter. */
        arguments.add (new VariableExpression (context, context.getRootScope ().getVariable ("model"),
                AccessMode.get, getLine ()));

        /* "Load" captured variables. */
        for (Variable capturedVariable : closureType.getClosureScope ().getCapturedVariables ()) {
            arguments.add (new VariableExpression (context, capturedVariable, AccessMode.get, getLine ()));
        }

        new ObjectCompileHelper (mv, new Type (closureType.getClosureClass ()))
                .compileNew (closureType.getConstructorDesc (), arguments);
        // -> Closure
    }

    public ClosureType getClosureType () {
        return closureType;
    }

}
