package io.collap.bryg;

import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import javax.annotation.Nullable;
import java.io.Writer;

/**
 * Implementations of the template interface must be thread-safe, as only a single object is used to render all instances
 * of the template.
 */
public interface Template {

    /**
     * @param model May be null if no parameters are expected.
     * @throws InvalidInputParameterException Thrown when the Model does not contain a variable.
     * @throws java.lang.ClassCastException Thrown when the supplied variable in the Model has a type that can not be
     *                                      casted to the expected type.
     */
    public void render (Writer writer, @Nullable Model model) throws InvalidInputParameterException, ClassCastException;

}
