package com.scolaris.exception;

public class ScolarisException extends RuntimeException {
    public ScolarisException(String message) {
        super(message);
    }

    public ScolarisException(String message, Throwable cause) {
        super(message, cause);
    }
}
