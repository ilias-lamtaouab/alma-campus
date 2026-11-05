package com.scolaris.exception;

public class DatabaseException extends ScolarisException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
