package com.finalbid.user.exception;

/**
 * Thrown when email verification token is invalid or expired.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
