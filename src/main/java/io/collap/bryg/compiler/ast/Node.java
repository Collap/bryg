package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.StandardVisitor;

import java.io.PrintStream;

public abstract class Node {

    protected StandardVisitor visitor;

    protected Node (StandardVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * IMPORTANT NOTE: All (named) variables *must* be resolved in the Node constructors. Using any "current" Scope in the `compile`
     * method yields undefined behaviour! The root scope can be used to allocate space for anonymous temporal variables.
     */
    public abstract void compile ();

    public void print (PrintStream out, int depth) {
        for (int i = 0; i < depth * 2; ++i) {
            out.print (' ');
        }
        out.println (getClass ().getSimpleName ());
    }

}
