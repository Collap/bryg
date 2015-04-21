package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.parser.BrygParser;

import java.util.List;

public class FunctionBodyNode extends InnerNode {

    public FunctionBodyNode(CompilationContext compilationContext, List<BrygParser.StatementContext> statementContexts) {
        super(compilationContext, Node.UNKNOWN_LINE);

        for (BrygParser.StatementContext sc : statementContexts) {
            children.add(new StatementNode(compilationContext, sc));
        }
    }

}
