package io.collap.bryg;

import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;
import java.io.Writer;

/**
 * A unit is the common ground of templates and blocks.
 */
public interface Unit {

    /**
     * @see Unit#render(java.io.Writer, io.collap.bryg.model.Model)
     */
    public void render (Writer writer);

    /**
     * @throws io.collap.bryg.exception.InvalidInputParameterException Thrown when the Model does not contain a variable.
     * @throws java.lang.ClassCastException Thrown when the supplied variable in the Model has a type that can not be
     *                                      casted to the expected type.
     */
    public void render (Writer writer, @Nullable Model model) throws InvalidInputParameterException, ClassCastException;

}
