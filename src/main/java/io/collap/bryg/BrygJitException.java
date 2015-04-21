package io.collap.bryg;

public final class BrygJitException extends RuntimeException {

    public BrygJitException(String message, int line) {
        super(buildMessage(message, line));
    }

    public BrygJitException(String message, int line, Throwable cause) {
        super(buildMessage(message, line), cause);
    }

    private static String buildMessage(String message, int line) {
        return "Error on line " + line + ": " + message;
    }

}
