package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;

public class DelegatorRootNode extends InnerNode {

    public DelegatorRootNode (CompilationContext compilationContext) {
        super (compilationContext, -1); // Delegators don't have corresponding source.
    }

    public void addChild (Node child) {
        children.add (child);
    }

}
