package io.collap.bryg.template;

import io.collap.bryg.compiler.expression.ClassType;

/**
 * This exception is thrown when a parameter is either null or has the wrong type.
 */
public class InvalidInputParameterException extends Exception {

    public static final ClassType CLASS_TYPE = new ClassType (InvalidInputParameterException.class);

    public InvalidInputParameterException (String message) {
        super (message);
    }

}
