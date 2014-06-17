package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class RootNode extends InnerNode {

    public RootNode (RenderVisitor visitor, BrygParser.StartContext ctx) {
        super (visitor);

        List<BrygParser.InDeclarationContext> inDeclarationContexts = ctx.inDeclaration ();
        for (BrygParser.InDeclarationContext idc : inDeclarationContexts) {
            InDeclarationNode node = visitor.visitInDeclaration (idc);
            if (node != null) {
                children.add (node);
            }
        }

        List<BrygParser.StatementContext> statementContexts = ctx.statement ();
        for (BrygParser.StatementContext sc : statementContexts) {
            children.add (new StatementNode (visitor, sc));
        }
    }

}
