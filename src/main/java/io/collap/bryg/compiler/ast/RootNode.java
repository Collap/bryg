package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.RootScope;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

public class RootNode extends InnerNode {

    private List<InDeclarationNode> inDeclarations = new ArrayList<> ();

    public RootNode (Context context, BrygParser.StartContext ctx) {
        this (context, ctx.inDeclaration (), ctx.statement ());
    }

    public RootNode (Context context, List<BrygParser.InDeclarationContext> inDeclarationContexts,
                     List<BrygParser.StatementContext> statementContexts) {
        super (context);

        for (BrygParser.InDeclarationContext idc : inDeclarationContexts) {
            InDeclarationNode node = context.getParseTreeVisitor ().visitInDeclaration (idc);
            if (node != null) {
                inDeclarations.add (node);
            }
        }

        for (BrygParser.StatementContext sc : statementContexts) {
            children.add (new StatementNode (context, sc));
        }
    }

    public void addGlobalVariableInDeclarations (RootScope scope) {
        for (String globalVariableName : scope.getGlobalVariablesUsed ()) {
            inDeclarations.add (new InDeclarationNode (context, scope.getVariable (globalVariableName), false, -1, true));
        }
    }

    @Override
    public void compile () {
        /* Compile in declarations. */
        for (InDeclarationNode inDeclaration : inDeclarations) {
            inDeclaration.compile ();
        }

        /* Compile statements. */
        super.compile ();
    }

}
