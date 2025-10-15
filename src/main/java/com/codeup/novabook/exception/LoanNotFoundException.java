package com.codeup.novabook.exception;

/**
 * Exception thrown when a loan is not found in the system.
 * <p>
 * This exception is typically thrown during loan lookup operations when
 * the requested loan cannot be located in the database. It extends
 * {@link BusinessException} as it represents a business logic condition.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class LoanNotFoundException extends BusinessException {
    
    /**
     * Constructs a new LoanNotFoundException with the standard error code.
     */
    public LoanNotFoundException() {
        super(ErrorCode.LOAN_NOT_FOUND);
    }

    /**
     * Constructs a new LoanNotFoundException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public LoanNotFoundException(Throwable cause) {
        super(ErrorCode.LOAN_NOT_FOUND, cause);
    }
}