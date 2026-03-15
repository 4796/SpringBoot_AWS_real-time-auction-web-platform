package com.finalbid.auction.exception;

public class BidValidationException extends RuntimeException {
    
    private final String errorCode;

    public BidValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
