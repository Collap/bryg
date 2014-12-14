package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.ast.expression.ModelLoadExpression;
import io.collap.bryg.compiler.ast.expression.VariableExpression;
import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.MethodScope;
import io.collap.bryg.compiler.scope.UnitScope;
import io.collap.bryg.compiler.scope.Variable;
import io.collap.bryg.parser.BrygParser;
import io.collap.bryg.unit.StandardUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RootNode extends InnerNode {

    private List<Node> globalLoadNodes;

    public RootNode (Context context, BrygParser.StartContext ctx) {
        this (context, ctx.statement ());
    }

    public RootNode (Context context, List<BrygParser.StatementContext> statementContexts) {
        super (context);

        globalLoadNodes = new ArrayList<> ();

        for (BrygParser.StatementContext sc : statementContexts) {
            children.add (new StatementNode (context, sc));
        }
    }

    public void addGlobalVariableLoads (MethodScope scope) {
        // TODO: The generated code uses a lot of GETFIELD operations. We can optimize that with DUP ops.

        Set<String> usedGlobals = scope.getGlobalVariablesUsed ();
        for (String name : usedGlobals) {
            Variable variable = scope.getVariable (name);
            UnitScope unitScope = context.getHighestLocalScope ().getUnitScope ();
            globalLoadNodes.add (new VariableExpression (context, -1, variable, AccessMode.set,
                    new ModelLoadExpression (context, variable.getInfo (), unitScope.getVariable (StandardUnit.GLOBALS_FIELD_NAME))));
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
