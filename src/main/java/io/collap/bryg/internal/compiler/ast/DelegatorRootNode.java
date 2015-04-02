package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.Context;

public class DelegatorRootNode extends InnerNode {

    public DelegatorRootNode (Context context) {
        super (context);
    }

    public void addChild (Node child) {
        children.add (child);
    }

}
