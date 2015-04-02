package io.collap.bryg.module;

import javax.annotation.Nullable;

/**
 * Functions and variables of modules <b>share a namespace</b>.
 */
public interface Module {

    public static enum Visibility {
        global, // Implicit
        module  // Explicit
    }

    /**
     * TODO: Comment.
     */
    public String getName();

    /**
     * TODO: Comment.
     */
    public Visibility getVisibility();

    /**
     * TODO: Comment.
     *
     * The implementation of this method must be <b>thread-safe</b>.
     */
    public @Nullable Member getMember (String name);

}
