package io.collap.bryg;

public final class BrygJitException extends RuntimeException {

    public BrygJitException(String message, int sourceLine) {
        super("Error on line " + sourceLine + ": " + message);
    }

}
