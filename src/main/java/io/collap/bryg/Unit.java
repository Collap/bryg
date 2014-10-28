package io.collap.bryg;

import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.IOException;
import java.io.Writer;

/**
 * A unit is the common ground of templates and closures.
 */
public interface Unit {

    /**
     * This method can be used when no parameters have to be set.
     * This only works when all parameters are optional.
     *
     * @see Unit#render(java.io.Writer, io.collap.bryg.model.Model)
     */
    public void render (Writer writer) throws IOException, InvalidInputParameterException, ClassCastException;

    /**
     * @throws io.collap.bryg.exception.InvalidInputParameterException Thrown when the Model does not contain a variable.
     * @throws java.lang.ClassCastException Thrown when the supplied variable in the Model has a type that can not be
     *                                      casted to the expected type.
     */
    public void render (Writer writer, Model model) throws IOException, InvalidInputParameterException, ClassCastException;

}
