package com.app.googlemaps.exception;

public class MaxLimitException extends Exception {
    private static final long serialVersionUID = 1L;

    public MaxLimitException() {
        super();
    }

    public MaxLimitException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public MaxLimitException(String arg0) {
        super(arg0);
    }
}
