package io.collap.bryg.environment;

import io.collap.bryg.Template;

public interface Environment {


    /**
     * The template name consists of two parts:
     *  - A prefix that is a valid Java package (e.g. io.collap.bryg).
     *  - A name that consists of zero or more package names and a Java class name (e.g. post.Edit, Simple).
     * The full template class name is then constructed by concatenating the prefix and name: prefix.name
     * This approach is chosen over a single name approach to facilitate quick loading from different sources,
     * while it provides every functionality that a single name solution can provide.
     */
    public Template getTemplate (String prefix, String name);

}
