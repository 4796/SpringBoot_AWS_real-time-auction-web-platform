package com.finalbid.user.exception;

/**
 * Thrown when attempting to register with an already-used username.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already in use: " + username);
    }
}
