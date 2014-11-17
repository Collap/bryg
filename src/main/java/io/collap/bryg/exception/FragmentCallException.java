package io.collap.bryg.exception;

/**
 * This exception is thrown by Unit.call if anything went wrong while calling the fragment.
 */
public class FragmentCallException extends RuntimeException {

    public FragmentCallException (String message) {
        super (message);
    }

    public FragmentCallException (String message, Throwable cause) {
        super (message, cause);
    }

}
