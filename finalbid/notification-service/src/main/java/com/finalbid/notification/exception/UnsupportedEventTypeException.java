package com.finalbid.notification.exception;

public class UnsupportedEventTypeException extends RuntimeException {
    public UnsupportedEventTypeException(String eventType) {
        super("Unsupported notification event type: " + eventType);
    }
}
