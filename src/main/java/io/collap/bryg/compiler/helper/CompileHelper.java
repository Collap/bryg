package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.parser.RenderVisitor;

public abstract class CompileHelper {

    protected RenderVisitor visitor;

    public CompileHelper (RenderVisitor visitor) {
        this.visitor = visitor;
    }

}
