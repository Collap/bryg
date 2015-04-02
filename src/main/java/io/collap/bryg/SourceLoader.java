package io.collap.bryg;

public interface SourceLoader {

    /**
     * @param name The proper <i>template name</i>, <b>not</b> a URI to the source.
     */
    public String getTemplateSource (String name);

}
