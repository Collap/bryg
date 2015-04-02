package io.collap.bryg;

import javax.annotation.Nullable;

// TODO: Some of these methods really don't fit to a public API. Potentially put these in StandardEnvironment.

/**
 * <p>An environment is an object that is responsible for loading, compiling and managing template classes.
 * Each environment has its own class loader, which means that multiple environments do not share namespaces.</p>
 *
 * <h3>Parent</h3>
 * <p>The parent environment is used when the environment built by this
 * class can't find a template. This makes it possible to share common
 * templates with multiple other environments without polluting the
 * namespace or compiling the template twice.</p>
 *
 * <h3>Source Loaders</h3>
 * <p>Source loaders find and load the source of a template based on its name.
 * The order of source loaders is important: If a source loader does not find a source, the next
 * source loader is polled, and so on. An environment implementation <b>must</b> guarantee this order.</p>
 *
 * <h3>Thread Safety</h3>
 * <p>Every implementation of this interface <b>guarantees thread safety</b>.</p>
 *
 * @see EnvironmentBuilder
 * @see io.collap.bryg.module.Module
 * @see io.collap.bryg.SourceLoader
 */
public interface Environment {

    /**
     * A single template can usually not be used in multiple different "contexts",
     * because a template has fields which need to be initialised. Hence, this method
     * returns a <b>thread-safe</b> template factory, which can be used to instantiate
     * the template type multiple times.
     *
     */
    public @Nullable TemplateFactory getTemplateFactory (String name);

}
