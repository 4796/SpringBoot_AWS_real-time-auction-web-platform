package com.finalbid.user.exception;

/**
 * Thrown when a user is not found by username or ID.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
