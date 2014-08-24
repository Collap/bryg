package io.collap.bryg.compiler.helper;

import io.collap.bryg.compiler.bytecode.BrygMethodVisitor;

public abstract class CompileHelper {

    protected BrygMethodVisitor mv;

    public CompileHelper (BrygMethodVisitor mv) {
        this.mv = mv;
    }

}
