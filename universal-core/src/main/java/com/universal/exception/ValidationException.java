package com.universal.exception;

public class ValidationException extends UniversalException {

    private static final long serialVersionUID = -5353586491332224106L;

    public ValidationException(final String debug) {
        super(debug);
    }

    public ValidationException(final Code code) {
        super(code);
    }

    public ValidationException(final Code code, final String debug) {
        super(code, debug);
    }

    public ValidationException(final Code code, final String message, final String debug) {
        super(code, message, debug);
    }

    public ValidationException(Throwable exception) {
        super(exception);
    }

    public ValidationException(final String debug, final Throwable exception) {
        super(debug, exception);
    }

    public ValidationException(final Code code, final Throwable exception) {
        super(code, exception);
    }

    public ValidationException(final Code code, final Throwable exception, final String debug) {
        super(code, exception, debug);
    }

    public ValidationException(final Code code, final String message, final Throwable exception, final String debug) {
        super(code, message, exception, debug);
    }
}
