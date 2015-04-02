package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.Context;

import java.io.PrintStream;

public abstract class Node {

    protected Context context;

    /**
     * A line of -1 indicates that the line has not been set!
     */
    private int line = -1;

    protected Node (Context context) {
        this.context = context;
    }

    /**
     * IMPORTANT NOTE: All (named) variables <b>must</b> be resolved in the Node constructors. Using any "current" Scope
     * in the `compile` method yields undefined behaviour! The root scope can be used to allocate space for anonymous
     * temporal variables.
     */
    public abstract void compile ();

    public void print (PrintStream out, int depth) {
        /* Print line number with indent. */
        String lineStr;
        if (line > 0) {
            lineStr = line + ":  ";
        }else {
            lineStr = "   ";
        }
        for (int i = lineStr.length (); i < 7; ++i) {
            out.print (' ');
        }
        out.print (lineStr);

        /* Print depth node indent. */
        for (int i = 0; i < depth; ++i) {
            out.print ("  ");
        }

        /* Print class name. */
        out.println (getClass ().getSimpleName ());
    }

    public int getLine () {
        return line;
    }

    protected void setLine (int line) {
        this.line = line;
    }

}
