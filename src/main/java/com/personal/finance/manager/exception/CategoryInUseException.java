package com.personal.finance.manager.exception;

/**
 * Exception thrown when attempting to delete a category that is still referenced by transactions.
 */
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
