package com.codeup.novabook.exception;

/**
 * Exception thrown when trying to close a loan that is already closed.
 * <p>
 * This exception is thrown when attempting to perform operations on a loan
 * that require it to be in an open state, but the loan has already been
 * closed. It extends {@link BusinessException} as it represents a business
 * logic violation.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class LoanAlreadyClosedException extends BusinessException {
    
    /**
     * Constructs a new LoanAlreadyClosedException with the standard error code.
     */
    public LoanAlreadyClosedException() {
        super(ErrorCode.LOAN_ALREADY_CLOSED);
    }

    /**
     * Constructs a new LoanAlreadyClosedException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public LoanAlreadyClosedException(Throwable cause) {
        super(ErrorCode.LOAN_ALREADY_CLOSED, cause);
    }
}