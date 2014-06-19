package io.collap.bryg.loader;

public interface SourceLoader {

    /**
     * @param name The proper <b>template</b> name (without the prefix) as described in {@link io.collap.bryg.environment.Environment},
     *             <b>not</b> a URI to the source.
     */
    public String getTemplateSource (String name);

}
