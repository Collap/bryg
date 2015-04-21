package io.collap.bryg.internal.compiler.ast;

import io.collap.bryg.internal.compiler.CompilationContext;

import java.io.PrintStream;

public abstract class Node {

    /**
     * Indicates that the line of the node is not known.
     * Using this should be avoided where possible, but in some cases it might
     * just not be feasible to attach a source line to some node in the AST.
     */
    public static final int UNKNOWN_LINE = -1;

    protected CompilationContext compilationContext;

    private int line;

    protected Node(CompilationContext compilationContext, int line) {
        this.compilationContext = compilationContext;
        this.line = line;
    }

    /**
     * <b>IMPORTANT NOTE</b>: All (named) variables <b>must</b> be resolved in the Node constructors. Using any
     * "current" Scope in the `compile` method yields undefined behaviour! The root scope can be used to allocate
     * space for anonymous temporal variables.
     */
    public abstract void compile();

    public void print(PrintStream out, int depth) {
        /* Print line number with indent. */
        String lineStr;
        if (line > 0) {
            lineStr = line + ":  ";
        } else {
            lineStr = "   ";
        }
        for (int i = lineStr.length(); i < 7; ++i) {
            out.print(' ');
        }
        out.print(lineStr);

        /* Print depth node indent. */
        for (int i = 0; i < depth; ++i) {
            out.print("  ");
        }

        /* Print class name. */
        out.println(getClass().getSimpleName());
    }

    public int getLine() {
        return line;
    }

}
