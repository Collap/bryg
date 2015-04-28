package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.LocalScope;
import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class BlockNode extends InnerNode {

    public BlockNode(CompilationContext compilationContext, BrygParser.BlockContext ctx) {
        super(compilationContext, ctx.getStart().getLine());
        addChildrenWithScope(ctx.children, compilationContext.getCurrentScope().createSubScope());
    }

    public BlockNode(CompilationContext compilationContext, BrygParser.ClosureBodyContext ctx) {
        super(compilationContext, ctx.getStart().getLine());
        addChildrenWithScope(ctx.children, compilationContext.getCurrentScope().createSubScope());
    }

    protected void addChildrenWithScope(List<ParseTree> trees, LocalScope scope) {
        LocalScope previousScope = compilationContext.getCurrentScope();
        compilationContext.setCurrentScope(scope);
        for (ParseTree tree : trees) {
            if (tree instanceof BrygParser.StatementContext) {
                children.add(new StatementNode(compilationContext, ((BrygParser.StatementContext) tree)));
            }
        }
        compilationContext.setCurrentScope(previousScope);
    }

}
