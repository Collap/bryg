package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.ast.expression.ModelLoadExpression;
import io.collap.bryg.internal.compiler.ast.expression.VariableExpression;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.FunctionScope;
import io.collap.bryg.internal.UnitScope;
import io.collap.bryg.internal.scope.Variable;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.internal.StandardUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RootNode extends InnerNode {

    private List<Node> globalLoadNodes;

    public RootNode (CompilationContext compilationContext, BrygParser.StartContext ctx) {
        this (compilationContext, ctx.statement ());
    }

    public RootNode (CompilationContext compilationContext, List<BrygParser.StatementContext> statementContexts) {
        super (compilationContext);

        globalLoadNodes = new ArrayList<> ();

        for (BrygParser.StatementContext sc : statementContexts) {
            children.add (new StatementNode (compilationContext, sc));
        }
    }

    public void addGlobalVariableLoads (FunctionScope scope) {
        // TODO: The generated code uses a lot of GETFIELD operations. We can optimize that with DUP ops.

        Set<String> usedGlobals = scope.getGlobalVariablesUsed ();
        for (String name : usedGlobals) {
            Variable variable = scope.getVariable (name);
            UnitScope unitScope = compilationContext.getFragmentScope().getUnitScope ();
            globalLoadNodes.add (new VariableExpression (compilationContext, -1, variable, AccessMode.set,
                    new ModelLoadExpression (compilationContext, variable.getInfo (), unitScope.getVariable (StandardUnit.GLOBALS_FIELD_NAME))));
        }
    }

    @Override
    public void compile () {
        /* Compile global load nodes. */
        for (Node node : globalLoadNodes) {
            node.compile ();
        }

        /* Compile statements. */
        super.compile ();
    }

}
