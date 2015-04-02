package io.collap.bryg.internal.compiler.util;

import io.collap.bryg.internal.compiler.BrygMethodVisitor;

public abstract class CompileHelper {

    protected BrygMethodVisitor mv;

    public CompileHelper (BrygMethodVisitor mv) {
        this.mv = mv;
    }

}
