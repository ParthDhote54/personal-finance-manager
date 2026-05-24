package com.personal.finance.manager.exception;

/**
 * Exception thrown when a user attempts to access a resource they do not own or have permission for.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
