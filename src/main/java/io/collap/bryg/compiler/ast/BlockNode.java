package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.StandardVisitor;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockNode extends InnerNode {

    // TODO: Add Scope semantics to each block! (And single statement, so merging blocks and single statements in the AST would be good)

    public BlockNode (StandardVisitor visitor, BrygParser.BlockContext ctx) {
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
    public BlockNode (StandardVisitor visitor) {
        super (visitor);
    }

}
