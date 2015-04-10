package io.collap.bryg;

import io.collap.bryg.module.Module;

/**
 * An EnvironmentBuilder instantiates and configures a single Environment instance.
 *
 * @see io.collap.bryg.Environment
 */
public interface EnvironmentBuilder {

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
     * <p><b>Mandatory</b>: Sets the class resolver of the environment.</p>
     *
     * @see ClassResolver
     */
    public void setClassResolver(ClassResolver classResolver);

    /**
     * <p><b>Optional</b>: Allows you to set the debug configuration of the environment. If the configuration is not
     * set, a standard configuration with all debug options off is chosen.</p>
     *
     * @see io.collap.bryg.DebugConfiguration
     */
    public void setDebugConfiguration(DebugConfiguration debugConfiguration);

    /**
     * <p>Builds and returns the environment.</p>
     *
     * <p>After this method has been called <b>once</b>, the behaviour of this object is <b>undefined</b>. Thus, it
     * is only possible to create a <b>single instance</b> of an Environment with each EnvironmentBuilder.</p>
     */
    public Environment build();

}
