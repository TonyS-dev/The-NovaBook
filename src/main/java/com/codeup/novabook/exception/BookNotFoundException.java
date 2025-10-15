package com.codeup.novabook.exception;

/**
 * Exception thrown when a book is not found in the system.
 * <p>
 * This exception is typically thrown during book lookup operations when
 * the requested book cannot be located in the database. It extends
 * {@link BusinessException} as it represents a business logic condition.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class BookNotFoundException extends BusinessException {
    
    /**
     * Constructs a new BookNotFoundException with the standard error code.
     */
    public BookNotFoundException() {
        super(ErrorCode.BOOK_NOT_FOUND);
    }

    /**
     * Constructs a new BookNotFoundException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public BookNotFoundException(Throwable cause) {
        super(ErrorCode.BOOK_NOT_FOUND, cause);
    }
}