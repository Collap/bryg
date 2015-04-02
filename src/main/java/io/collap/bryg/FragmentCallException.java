package io.collap.bryg;

/**
 * This exception is thrown by Unit.call if anything went wrong while calling the fragment.
 */
public final class FragmentCallException extends RuntimeException {

    public FragmentCallException (String message) {
        super (message);
    }

    public FragmentCallException (String message, Throwable cause) {
        super (message, cause);
    }

}
