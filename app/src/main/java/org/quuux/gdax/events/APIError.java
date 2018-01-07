package org.quuux.gdax.events;


public class APIError {

    public int status;
    public String message;

    public APIError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public APIError(String message) {
        this(0, message);
    }

    public APIError(int status) {
        this(status, null);
    }
}
