package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.context.Context;

public class DelegatorRootNode extends InnerNode {

    public DelegatorRootNode (Context context) {
        super (context);
    }

    public void addChild (Node child) {
        children.add (child);
    }

}
