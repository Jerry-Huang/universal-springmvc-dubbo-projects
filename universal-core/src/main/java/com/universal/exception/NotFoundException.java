package com.universal.exception;

public class NotFoundException extends UniversalException {

    private static final long serialVersionUID = 4091465232123693535L;

    public NotFoundException(final String debug) {
        super(debug);
    }

    public NotFoundException(final Code code) {
        super(code);
    }

    public NotFoundException(final Code code, final String debug) {
        super(code, debug);
    }

    public NotFoundException(final Code code, final String message, final String debug) {
        super(code, message, debug);
    }

    public NotFoundException(Throwable exception) {
        super(exception);
    }

    public NotFoundException(final String debug, final Throwable exception) {
        super(debug, exception);
    }

    public NotFoundException(final Code code, final Throwable exception) {
        super(code, exception);
    }

    public NotFoundException(final Code code, final Throwable exception, final String debug) {
        super(code, exception, debug);
    }

    public NotFoundException(final Code code, final String message, final Throwable exception, final String debug) {
        super(code, message, exception, debug);
    }
}
