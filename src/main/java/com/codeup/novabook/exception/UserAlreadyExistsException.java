package com.codeup.novabook.exception;

/**
 * Exception thrown when trying to register a user with an email that already exists.
 * <p>
 * This exception is thrown during user registration when the system detects
 * that a user with the same email address is already registered. It extends
 * {@link BusinessException} as it represents a business rule violation.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class UserAlreadyExistsException extends BusinessException {
    
    /**
     * Constructs a new UserAlreadyExistsException with the standard error code.
     */
    public UserAlreadyExistsException() {
        super(ErrorCode.USER_ALREADY_EXISTS);
    }

    /**
     * Constructs a new UserAlreadyExistsException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public UserAlreadyExistsException(Throwable cause) {
        super(ErrorCode.USER_ALREADY_EXISTS, cause);
    }
}