package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.parser.BrygMethodVisitor;

public abstract class CompileHelper {

    protected BrygMethodVisitor method;

    public CompileHelper (BrygMethodVisitor method) {
        this.method = method;
    }

}
