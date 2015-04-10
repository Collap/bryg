package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Scope;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockNode extends InnerNode {

    public BlockNode (CompilationContext compilationContext, BrygParser.BlockContext ctx) {
        super (compilationContext);

        addChildrenWithScope (ctx, compilationContext.getCurrentScope ().createSubScope ());
    }

    protected void addChildrenWithScope (BrygParser.BlockContext ctx, Scope scope) {
        compilationContext.setCurrentScope (scope);

        for (ParseTree tree : ctx.children) {
            if (tree instanceof BrygParser.StatementContext) {
                children.add (new StatementNode (compilationContext, ((BrygParser.StatementContext) tree)));
            }
        }

        compilationContext.setCurrentScope (scope.getParent ());
    }

    /**
     * Creates an empty block.
     */
    public BlockNode (CompilationContext compilationContext) {
        super (compilationContext);
    }

}
