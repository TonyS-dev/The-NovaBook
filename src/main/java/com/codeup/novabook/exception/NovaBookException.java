package com.codeup.novabook.exception;

/**
 * Base exception class for the NovaBook application.
 * <p>
 * This is the root exception class that provides a standardized approach to
 * error handling throughout the NovaBook system. It integrates with the
 * {@link ErrorCode} enum to provide consistent error codes, messages, and
 * severity levels across all application layers.
 * </p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Standardized error codes and messages via {@link ErrorCode}</li>
 *   <li>Severity classification for better error handling</li>
 *   <li>Support for exception chaining</li>
 *   <li>Formatted error representation</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Simple usage with error code
 * throw new NovaBookException(ErrorCode.USER_NOT_FOUND);
 * 
 * // With underlying cause
 * throw new NovaBookException(ErrorCode.DATABASE_ERROR, sqlException);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see ErrorCode
 * @see Severity
 */
public abstract class NovaBookException extends RuntimeException {
    /**
     * The error code associated with this exception.
     */
    private final ErrorCode errorCode;

    /**
     * Constructs a new NovaBookException with the specified error code.
     * 
     * @param errorCode the error code that describes this exception
     */
    protected NovaBookException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new NovaBookException with the specified error code and cause.
     * 
     * @param errorCode the error code that describes this exception
     * @param cause the underlying cause of this exception
     */
    protected NovaBookException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code
     */
    public ErrorCode getErrorCode() { 
        return errorCode; 
    }

    /**
     * Gets the unique error code identifier.
     * 
     * @return the error code string
     */
    public String getCode() { 
        return errorCode.getCode(); 
    }

    /**
     * Gets the severity level of this exception.
     * 
     * @return the severity level
     */
    public Severity getSeverity() { 
        return errorCode.getSeverity(); 
    }

    /**
     * Returns a formatted string representation of this exception.
     * 
     * @return formatted exception information including severity, code, and message
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s",
                errorCode.getSeverity(), errorCode.getCode(), getMessage());
    }
}