package com.finalbid.user.exception;

/**
 * Thrown when attempting to register with an already-used email.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
