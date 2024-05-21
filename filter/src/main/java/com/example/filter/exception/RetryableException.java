package com.example.filter.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }

}
