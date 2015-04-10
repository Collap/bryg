package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class InnerNode extends Node {

    protected List<Node> children = new ArrayList<> ();

    protected InnerNode (CompilationContext compilationContext) {
        super (compilationContext);
    }

    @Override
    public void compile () {
        for (Node child : children) {
            child.compile ();
        }
    }

    @Override
    public void print (PrintStream out, int depth) {
        super.print (out, depth);
        for (Node child : children) {
            child.print (out, depth + 1);
        }
    }

}
