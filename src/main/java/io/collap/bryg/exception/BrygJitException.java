package io.collap.bryg.exception;

public class BrygJitException extends RuntimeException {

    public BrygJitException (String message, int sourceLine) {
        super ("Error on line " + sourceLine + ": " + message);
    }

}
