package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.context.Context;
import io.collap.bryg.compiler.scope.Scope;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockNode extends InnerNode {

    public BlockNode (Context context, BrygParser.BlockContext ctx) {
        super (context);

        Scope scope = context.getCurrentScope ().createSubScope ();
        context.setCurrentScope (scope);

        for (ParseTree tree : ctx.children) {
            if (tree instanceof BrygParser.StatementContext) {
                children.add (new StatementNode (context, ((BrygParser.StatementContext) tree)));
            }
        }

        context.setCurrentScope (scope.getParent ());
    }

    /**
     * Creates an empty block.
     */
    public BlockNode (Context context) {
        super (context);
    }

}
