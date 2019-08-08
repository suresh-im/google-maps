package com.app.googlemaps.ws.client;

public class WSClientException extends Exception {
    private static final long serialVersionUID = 1L;

    public WSClientException() {
        super();
    }

    public WSClientException(String message) {
        super(message);
    }

    public WSClientException(String message, Throwable cause) {
        super(message, cause);
    }
}