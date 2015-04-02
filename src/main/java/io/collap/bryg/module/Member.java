package io.collap.bryg.module;

import io.collap.bryg.internal.compiler.ast.Node;
import io.collap.bryg.internal.compiler.Context;
import io.collap.bryg.internal.Type;

/**
 * Any subclass of this class <b>must</b> be thread-safe.
 */
public interface Member<T extends Node> {

    /**
     * TODO: Comment.
     */
    public abstract void compile(Context context, T node);

    /**
     * TODO: Comment.
     */
    public abstract Type getResultType();

}
