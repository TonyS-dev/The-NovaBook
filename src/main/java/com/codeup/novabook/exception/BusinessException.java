package com.codeup.novabook.exception;

/**
 * Base class for business logic exceptions in the NovaBook system.
 * <p>
 * This exception class represents errors that occur due to business rule
 * violations, invalid data, or logical inconsistencies. These are typically
 * recoverable errors that can be communicated to the user for correction.
 * </p>
 * 
 * <p><strong>Common Business Exception Scenarios:</strong></p>
 * <ul>
 *   <li>Data validation failures</li>
 *   <li>Business rule violations</li>
 *   <li>Resource not found conditions</li>
 *   <li>Duplicate resource conflicts</li>
 *   <li>State transition errors</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 2.0
 */
public class BusinessException extends NovaBookException {

    /**
     * Constructs a new BusinessException with the specified error code.
     * 
     * @param errorCode the error code that describes this business exception
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new BusinessException with the specified error code and cause.
     * 
     * @param errorCode the error code that describes this business exception
     * @param cause the underlying cause of this exception
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}