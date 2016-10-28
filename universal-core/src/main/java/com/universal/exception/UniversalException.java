package com.universal.exception;

import org.apache.commons.lang3.StringUtils;

public class UniversalException extends Exception {

    private static final long serialVersionUID = -5411785480261545811L;

    private Code code;
    private String message;

    public UniversalException(final String debug) {
        super(debug);
    }

    public UniversalException(final Code code) {
        this(code, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public UniversalException(final Code code, final String debug) {
        this(code, StringUtils.EMPTY, debug);
    }

    public UniversalException(final Code code, final String message, final String debug) {
        super(debug);
        this.code = code;
        this.message = message;
    }

    public UniversalException(Throwable exception) {
        super(exception);
    }

    public UniversalException(final String debug, final Throwable exception) {
        super(debug, exception);
    }

    public UniversalException(final Code code, final Throwable exception) {
        this(code, StringUtils.EMPTY, exception, StringUtils.EMPTY);
    }

    public UniversalException(final Code code, final Throwable exception, final String debug) {
        this(code, StringUtils.EMPTY, exception, debug);
    }

    public UniversalException(final Code code, final String message, final Throwable exception, final String debug) {
        super(debug, exception);
        this.code = code;
        this.message = message;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public String getDebug() {
        return super.getMessage();
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {

        String result = StringUtils.EMPTY;

        if (this.code != null) {
            result = "[" + this.code.toString() + "]";
        }

        String debug = super.getMessage();

        if (StringUtils.isNotBlank(debug)) {
            result += ", " + debug;
        }

        return result;
    }
}
