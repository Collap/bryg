package io.collap.bryg;

import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.Writer;

/**
 * Implementations of the template interface are allowed to have fields, as a new template object must be created for
 * each call to Environment.getTemplate.
 * The same template object (with the same fields) an be used to render multiple models; This means that the user
 * has to ensure that a template object is reused in the correct manner.
 */
public interface Template {

    /**
     * @throws InvalidInputParameterException Thrown when the Model does not contain a variable.
     * @throws java.lang.ClassCastException Thrown when the supplied variable in the Model has a type that can not be
     *                                      casted to the expected type.
     */
    public void render (Writer writer, Model model) throws InvalidInputParameterException, ClassCastException;

}
