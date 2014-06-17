package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.RenderVisitor;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockNode extends InnerNode {

    public BlockNode (RenderVisitor visitor, BrygParser.BlockContext ctx) {
        super (visitor);

        for (ParseTree tree : ctx.children) {
            if (tree instanceof BrygParser.StatementLineContext) {
                children.add (new StatementNode (visitor, ((BrygParser.StatementLineContext) tree).statement ()));
            }
        }
    }

    /**
     * Creates an empty block.
     */
    public BlockNode (RenderVisitor visitor) {
        super (visitor);
    }

}
