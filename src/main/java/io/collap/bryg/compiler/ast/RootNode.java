package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class RootNode extends InnerNode {

    public RootNode (StandardVisitor visitor, BrygParser.StartContext ctx) {
        super (visitor);

        List<BrygParser.InDeclarationContext> inDeclarationContexts = ctx.inDeclaration ();
        for (BrygParser.InDeclarationContext idc : inDeclarationContexts) {
            InDeclarationNode node = visitor.visitInDeclaration (idc);
            if (node != null) {
                children.add (node);
            }
        }

        List<BrygParser.StatementLineContext> statementLineContexts = ctx.statementLine ();
        for (BrygParser.StatementLineContext sc : statementLineContexts) {
            children.add (new StatementNode (visitor, sc.statement ()));
        }
    }

}
