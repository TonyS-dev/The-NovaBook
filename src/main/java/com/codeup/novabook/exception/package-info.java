/**
 * Exception hierarchy for the EcoFleet application.
 * <p>
 * This package contains all custom exceptions used throughout the EcoFleet system.
 * All exceptions extend from {@link com.codeup.NovaBookException.exception.EcoFleetException}
 * which serves as the base exception class for the application.
 * </p>
 * 
 * <p><strong>Exception Categories:</strong></p>
 * <ul>
 * <li><strong>Validation Exceptions:</strong> {@link com.codeup.novabook.exception.ValidationException} 
 *     for input validation failures</li>
 * <li><strong>Authentication Exceptions:</strong> {@link com.codeup.novabook.exception.AuthenticationException} 
 *     for login and credential issues</li>
 * <li><strong>Entity Not Found:</strong> Various *NotFoundException classes for missing entities</li>
 * <li><strong>Business Rule Violations:</strong> Exceptions for business logic violations</li>
 * <li><strong>Database Exceptions:</strong> {@link com.codeup.novabook.exception.DatabaseException} 
 *     for data access issues</li>
 * </ul>
 * 
 * <p>
 * All exceptions provide meaningful error messages and support chaining of underlying causes.
 * They follow the checked exception pattern to ensure proper error handling throughout the application.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
package com.codeup.novabook.exception;