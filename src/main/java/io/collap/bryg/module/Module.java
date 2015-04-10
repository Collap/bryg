package io.collap.bryg.module;

import io.collap.bryg.Visibility;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Functions and variables of modules <b>share a namespace</b>.
 *
 * Once a member is registered with a module, it must not be removed.
 */
public interface Module {

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
    public @Nullable Member<?> getMember (String name);

    /**
     * TODO: Comment.
     *
     * As this method is only used during the initialization process of an environment,
     * the implementation of this method does not need to be thread-safe.
     *
     * It is preferable to return an Iterator that operates on an immutable collection,
     * since a Module should not support removing any members.
     */
    public Iterator<? extends Member<?>> getMemberIterator();

}
