package io.collap.bryg;

import java.io.Writer;

/**
 * A unit is the common ground of templates and closures.
 */
public interface Unit {

    /**
     * This method passes an empty model.
     *
     * @see Unit#call(java.lang.String, java.io.Writer, Model)
     */
    public void call (String name, Writer writer) throws FragmentCallException;

    /**
     * Calls the fragment function referenced by 'name'.
     */
    public void call (String name, Writer writer, Model model) throws FragmentCallException;

}
