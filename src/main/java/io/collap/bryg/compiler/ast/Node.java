package io.collap.bryg.compiler.ast;

import io.collap.bryg.compiler.parser.StandardVisitor;

import java.io.PrintStream;
import java.util.Map;

public abstract class Node {

    protected StandardVisitor visitor;
    private int line = 0;

    protected Node (StandardVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * IMPORTANT NOTE: All (named) variables *must* be resolved in the Node constructors. Using any "current" Scope in the `compile`
     * method yields undefined behaviour! The root scope can be used to allocate space for anonymous temporal variables.
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
        /* Convert prep line to source line. */
        Map<Integer, Integer> lineToSourceLineMap = visitor.getLineToSourceLineMap ();
        Integer sourceLine = lineToSourceLineMap.get (line);
        if (sourceLine == null) {
            sourceLine = 0;
        }
        this.line = sourceLine;
    }

}
