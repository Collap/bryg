package io.collap.bryg;

public class DebugConfiguration {

    private boolean shouldPrintTokens;
    private boolean shouldPrintParseTree;
    private boolean shouldPrintBytecode;
    private boolean shouldPrintAst;

    public DebugConfiguration(boolean shouldPrintTokens, boolean shouldPrintParseTree, boolean shouldPrintBytecode, boolean shouldPrintAst) {
        this.shouldPrintTokens = shouldPrintTokens;
        this.shouldPrintParseTree = shouldPrintParseTree;
        this.shouldPrintBytecode = shouldPrintBytecode;
        this.shouldPrintAst = shouldPrintAst;
    }

    public boolean shouldPrintTokens() {
        return shouldPrintTokens;
    }

    public void setShouldPrintTokens(boolean shouldPrintTokens) {
        this.shouldPrintTokens = shouldPrintTokens;
    }

    public boolean shouldPrintParseTree() {
        return shouldPrintParseTree;
    }

    public void setShouldPrintParseTree(boolean shouldPrintParseTree) {
        this.shouldPrintParseTree = shouldPrintParseTree;
    }

    public boolean shouldPrintBytecode() {
        return shouldPrintBytecode;
    }

    public void setShouldPrintBytecode(boolean shouldPrintBytecode) {
        this.shouldPrintBytecode = shouldPrintBytecode;
    }

    public boolean shouldPrintAst() {
        return shouldPrintAst;
    }

    public void setShouldPrintAst(boolean shouldPrintAst) {
        this.shouldPrintAst = shouldPrintAst;
    }

}
