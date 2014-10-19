package io.collap.bryg;

import io.collap.bryg.parser.BrygParser;

public class CompilationData {

    private BrygParser.StartContext startContext;

    public CompilationData (BrygParser.StartContext startContext) {
        this.startContext = startContext;
    }

    public BrygParser.StartContext getStartContext () {
        return startContext;
    }

}
