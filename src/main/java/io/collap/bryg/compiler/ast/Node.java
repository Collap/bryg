package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.RenderVisitor;

import java.io.PrintStream;

public abstract class Node {

    protected RenderVisitor visitor;

    protected Node (RenderVisitor visitor) {
        this.visitor = visitor;
    }

    public abstract void compile ();

    public void print (PrintStream out, int depth) {
        for (int i = 0; i < depth * 2; ++i) {
            out.print (' ');
        }
        out.println (getClass ().getSimpleName ());
    }

}
