package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class RootNode extends InnerNode {

    public RootNode(CompilationContext compilationContext, List<BrygParser.StatementContext> statementContexts) {
        super(compilationContext);

        for (BrygParser.StatementContext sc : statementContexts) {
            children.add(new StatementNode(compilationContext, sc));
        }
    }

    @Override
    public void compile() {
        /* Compile statements. */
        super.compile();
    }

}
