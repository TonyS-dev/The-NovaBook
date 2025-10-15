package com.codeup.novabook.exception;

/**
 * Exception thrown when trying to rent a book that is not available.
 * <p>
 * This exception is thrown when attempting to create a rental for a book
 * that is currently unavailable (either already loaned or marked as unavailable).
 * It extends {@link BusinessException} as it represents a business rule violation.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class BookNotAvailableException extends BusinessException {
    
    /**
     * Constructs a new BookNotAvailableException with the standard error code.
     */
    public BookNotAvailableException() {
        super(ErrorCode.BOOK_NOT_AVAILABLE);
    }

    /**
     * Constructs a new BookNotAvailableException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public BookNotAvailableException(Throwable cause) {
        super(ErrorCode.BOOK_NOT_AVAILABLE, cause);
    }
}