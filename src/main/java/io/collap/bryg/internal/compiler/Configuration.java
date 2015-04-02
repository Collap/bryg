package io.collap.bryg.internal.compiler;

public class Configuration {

    private boolean printTokens = false;
    private boolean printParseTree = false;
    private boolean printAst = false;
    private boolean printBytecode = false;

    public boolean shouldPrintTokens () {
        return printTokens;
    }

    public void setPrintTokens (boolean printTokens) {
        this.printTokens = printTokens;
    }

    public boolean shouldPrintParseTree () {
        return printParseTree;
    }

    public void setPrintParseTree (boolean printParseTree) {
        this.printParseTree = printParseTree;
    }

    public boolean shouldPrintAst () {
        return printAst;
    }

    public void setPrintAst (boolean printAst) {
        this.printAst = printAst;
    }

    public boolean shouldPrintBytecode () {
        return printBytecode;
    }

    public void setPrintBytecode (boolean printBytecode) {
        this.printBytecode = printBytecode;
    }

}
