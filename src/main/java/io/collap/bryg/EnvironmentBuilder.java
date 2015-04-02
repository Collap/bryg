package io.collap.bryg;

import io.collap.bryg.module.Module;

/**
 * An EnvironmentBuilder instantiates and configures a single Environment instance.
 *
 * @see io.collap.bryg.Environment
 */
public interface EnvironmentBuilder {

    /**
     * <p><b>Optional</b>: Sets the unique parent of the environment.</p>
     *
     * For further information on environment parents, see {@link io.collap.bryg.Environment}.
     */
    public void setParent(Environment parent);

    /**
     * <p><b>Optional</b>: Registers a module with the environment.</p>
     *
     * @see io.collap.bryg.module.Module
     */
    public void registerModule(Module module);

    /**
     * <p><b>Mandatory</b>: Registers a source loader with the environment. At least one source loader must be registered.</p>
     *
     * <p>This method must guarantee that the order of registration is preserved. For further information,
     * see {@link io.collap.bryg.Environment}.</p>
     *
     * @see io.collap.bryg.SourceLoader
     */
    public void registerSourceLoader(SourceLoader sourceLoader);

    /**
     * <p><b>Mandatory</b>: Registers a class resolver with the environment. At least one class resolver must be registered.</p>
     *
     * <p>This method must guarantee that the order of registration is preserved. For further information,
     * see {@link io.collap.bryg.Environment}.</p>
     *
     * @see ClassResolver
     */
    public void registerClassResolver(ClassResolver classResolver);

    /**
     * <p>Builds and returns the environment.</p>
     *
     * <p>After this method has been called <b>once</b>, the behaviour of this object is <b>undefined</b>. Thus, it
     * is only possible to create a <b>single instance</b> of an Environment with each EnvironmentBuilder.</p>
     */
    public Environment build();

}
