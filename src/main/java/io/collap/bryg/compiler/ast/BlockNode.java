package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.expression.Scope;
import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockNode extends InnerNode {

    public BlockNode (StandardVisitor visitor, BrygParser.BlockContext ctx) {
        super (visitor);

        Scope scope = visitor.getCurrentScope ().createSubScope ();
        visitor.setCurrentScope (scope);

        for (ParseTree tree : ctx.children) {
            if (tree instanceof BrygParser.StatementLineContext) {
                children.add (new StatementNode (visitor, ((BrygParser.StatementLineContext) tree).statement ()));
            }
        }

        visitor.setCurrentScope (scope.getParent ());
    }

    /**
     * Creates an empty block.
     */
    public BlockNode (StandardVisitor visitor) {
        super (visitor);
    }

}
