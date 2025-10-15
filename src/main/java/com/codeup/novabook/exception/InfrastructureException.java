package com.codeup.novabook.exception;

/**
 * Base class for infrastructure-related exceptions in the NovaBook system.
 * <p>
 * This exception class represents errors that occur at the infrastructure level,
 * such as database connectivity issues, transaction failures, or external
 * service unavailability. These errors are typically not recoverable at the
 * application level and may require system administrator intervention.
 * </p>
 * 
 * <p><strong>Common Infrastructure Exception Scenarios:</strong></p>
 * <ul>
 *   <li>Database connection failures</li>
 *   <li>Transaction rollback errors</li>
 *   <li>Network connectivity issues</li>
 *   <li>File system access problems</li>
 *   <li>External service timeouts</li>
 *   <li>Configuration errors</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 2.0
 */
public class InfrastructureException extends NovaBookException {

    /**
     * Constructs a new InfrastructureException with the specified error code.
     * 
     * @param errorCode the error code that describes this infrastructure exception
     */
    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new InfrastructureException with the specified error code and cause.
     * 
     * @param errorCode the error code that describes this infrastructure exception
     * @param cause the underlying cause of this exception (typically a technical exception)
     */
    public InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}