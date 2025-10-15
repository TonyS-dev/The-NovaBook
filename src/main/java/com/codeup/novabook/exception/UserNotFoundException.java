package com.codeup.novabook.exception;

/**
 * Exception thrown when a user is not found in the system.
 * <p>
 * This exception is typically thrown during user lookup operations when
 * the requested user cannot be located in the database. It extends
 * {@link BusinessException} as it represents a business logic condition
 * rather than a technical failure.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class UserNotFoundException extends BusinessException {
    
    /**
     * Constructs a new UserNotFoundException with the standard error code.
     */
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * Constructs a new UserNotFoundException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public UserNotFoundException(Throwable cause) {
        super(ErrorCode.USER_NOT_FOUND, cause);
    }
}