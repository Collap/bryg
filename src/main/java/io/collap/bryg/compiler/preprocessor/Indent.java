package io.collap.bryg.compiler.preprocessor;

public class Indent {

    private int column;

    public Indent (int column) {
        this.column = column;
    }

    public int getColumn () {
        return column;
    }

}
