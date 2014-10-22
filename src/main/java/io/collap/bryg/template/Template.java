package io.collap.bryg.template;

import io.collap.bryg.Unit;

/**
 * Implementations of the template interface are allowed to have fields, as a new template object must be created for
 * each call to Environment.getTemplate.
 * The same template object (with the same fields) can be used to render multiple models; This means that the user
 * has to ensure that a template object is reused in the correct manner.
 */
public interface Template extends Unit {

}
