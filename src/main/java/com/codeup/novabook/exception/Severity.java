package com.codeup.novabook.exception;

/**
 * Enum representing different severity levels for exceptions.
 * <p>
 * This enum provides a standardized way to classify the severity of errors
 * throughout the EcoFleet system, enabling better error handling, logging,
 * and user experience management.
 * </p>
 * 
 * <p><strong>Severity Levels:</strong></p>
 * <ul>
 *   <li><strong>INFO</strong> - Informational messages, no action required</li>
 *   <li><strong>WARN</strong> - Warning conditions, system can continue</li>
 *   <li><strong>ERROR</strong> - Error conditions, operation failed but system stable</li>
 *   <li><strong>FATAL</strong> - Critical errors, system stability compromised</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 2.0
 */
public enum Severity {
    /**
     * Informational messages - typically for successful operations or status updates.
     */
    INFO,
    
    /**
     * Warning conditions - potential issues but system can continue operating.
     */
    WARN,
    
    /**
     * Error conditions - operation failed but system remains stable.
     */
    ERROR,
    
    /**
     * Fatal errors - critical system failures that may compromise stability.
     */
    FATAL
}