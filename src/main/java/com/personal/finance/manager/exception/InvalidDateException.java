package com.personal.finance.manager.exception;

/**
 * Exception thrown when dates are invalid, such as future transaction dates.
 */
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
}
