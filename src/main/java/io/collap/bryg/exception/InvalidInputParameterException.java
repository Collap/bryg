package io.collap.bryg.exception;

/**
 * This exception is thrown when a parameter is either null or has the wrong type.
 */
public class InvalidInputParameterException extends Exception {

    public InvalidInputParameterException (String message) {
        super (message);
    }

}
