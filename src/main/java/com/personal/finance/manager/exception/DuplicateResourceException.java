package com.personal.finance.manager.exception;

/**
 * Exception thrown when creating a resource that violates unique constraints, such as a duplicate username or category name.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
