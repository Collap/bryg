package io.collap.bryg.module;

import io.collap.bryg.internal.compiler.CompilationContext;
import io.collap.bryg.internal.Type;

/**
 * Any subclass of this class <b>must</b> be thread-safe.
 */
public interface Member<T> {

    /**
     * TODO: Comment.
     */
    public abstract String getName();

    /**
     * TODO: Comment.
     */
    public abstract void compile(CompilationContext compilationContext, T information);

    /**
     * TODO: Comment.
     */
    public abstract Type getResultType();

}
