package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class RootNode extends InnerNode {

    public RootNode (Context context, BrygParser.StartContext ctx) {
        super (context);

        List<BrygParser.InDeclarationContext> inDeclarationContexts = ctx.inDeclaration ();
        for (BrygParser.InDeclarationContext idc : inDeclarationContexts) {
            InDeclarationNode node = context.getParseTreeVisitor ().visitInDeclaration (idc);
            if (node != null) {
                children.add (node);
            }
        }

        List<BrygParser.StatementLineContext> statementLineContexts = ctx.statementLine ();
        for (BrygParser.StatementLineContext sc : statementLineContexts) {
            children.add (new StatementNode (context, sc.statement ()));
        }
    }

}
