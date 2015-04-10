package io.collap.bryg;

import javax.annotation.Nullable;

public interface SourceLoader {

    /**
     * @param name The proper <i>template name</i>, <b>not</b> a URI to the source.
     * @return The source of the template or <i>null</i> if the template source does not exist.
     */
    public @Nullable String getTemplateSource (String name);

}
