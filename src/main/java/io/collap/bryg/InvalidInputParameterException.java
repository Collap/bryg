package io.collap.bryg;

/**
 * This exception is thrown when a parameter is either null or has the wrong type.
 */
public final class InvalidInputParameterException extends RuntimeException {

    public InvalidInputParameterException (String message) {
        super (message);
    }

}
