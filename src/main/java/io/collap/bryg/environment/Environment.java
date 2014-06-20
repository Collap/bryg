package io.collap.bryg.environment;

import io.collap.bryg.Template;

/**
 * An implementation of Environment <b>must</b> be thread-safe. It must also be guaranteed that any SourceLoader or
 * TemplateClassLoader instances are used in a thread-safe manner, as these classes are not expected to be thread-safe
 * themselves.
 */
public interface Environment {

    public Template getTemplate (String name);

}
