package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;

public class DelegatorBodyNode extends InnerNode {

    public DelegatorBodyNode(CompilationContext compilationContext) {
        super(compilationContext, Node.UNKNOWN_LINE); // Delegators don't have corresponding source.
    }

    public void addChild(Node child) {
        children.add(child);
    }

}
