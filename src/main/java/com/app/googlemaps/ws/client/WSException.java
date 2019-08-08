package com.app.googlemaps.ws.client;

public class WSException extends Exception {
    private static final long serialVersionUID = 1L;

    public WSException() {
        super();
    }

    public WSException(String message) {
        super(message);
    }

    public WSException(String message, Throwable cause) {
        super(message, cause);
    }
}
