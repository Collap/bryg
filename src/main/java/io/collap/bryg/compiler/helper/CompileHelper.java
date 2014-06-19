package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.parser.StandardVisitor;

public abstract class CompileHelper {

    protected StandardVisitor visitor;

    public CompileHelper (StandardVisitor visitor) {
        this.visitor = visitor;
    }

}
