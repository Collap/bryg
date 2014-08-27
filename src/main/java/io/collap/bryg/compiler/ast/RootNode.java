package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.RootScope;
import io.collap.bryg.parser.BrygParser;

import java.util.ArrayList;
import java.util.List;

public class RootNode extends InnerNode {

    private List<InDeclarationNode> inDeclarations = new ArrayList<> ();

    public RootNode (Context context, BrygParser.StartContext ctx) {
        super (context);

        List<BrygParser.InDeclarationContext> inDeclarationContexts = ctx.inDeclaration ();
        for (BrygParser.InDeclarationContext idc : inDeclarationContexts) {
            InDeclarationNode node = context.getParseTreeVisitor ().visitInDeclaration (idc);
            if (node != null) {
                inDeclarations.add (node);
            }
        }

        List<BrygParser.StatementLineContext> statementLineContexts = ctx.statementLine ();
        for (BrygParser.StatementLineContext sc : statementLineContexts) {
            children.add (new StatementNode (context, sc.statement ()));
        }
    }

    public void addGlobalVariableInDeclarations (RootScope scope) {
        for (String globalVariableName : scope.getGlobalVariablesUsed ()) {
            inDeclarations.add (new InDeclarationNode (context, scope.getVariable (globalVariableName), -1));
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
