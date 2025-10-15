package com.codeup.novabook.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 * <p>
 * This exception is used throughout the EcoFleet system to indicate
 * authentication failures, such as incorrect email/password combinations
 * or expired sessions. It extends {@link BusinessException} as authentication
 * failures are user-correctable conditions.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class AuthenticationException extends BusinessException {
    
    /**
     * Constructs a new AuthenticationException with the standard authentication failed error code.
     */
    public AuthenticationException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }

    /**
     * Constructs a new AuthenticationException with a specific error code.
     * 
     * @param errorCode the specific authentication error code
     */
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new AuthenticationException with a specific error code and cause.
     * 
     * @param errorCode the specific authentication error code
     * @param cause the underlying cause of this exception
     */
    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    // Convenience factory methods
    
    /**
     * Creates an AuthenticationException for general authentication failure.
     * 
     * @return AuthenticationException with authentication failed error code
     */
    public static AuthenticationException forUser() {
        return new AuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
    }

    /**
     * Creates an AuthenticationException for invalid credentials.
     * 
     * @return AuthenticationException with invalid credentials error code
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
    }

    /**
     * Creates an AuthenticationException for expired session.
     * 
     * @return AuthenticationException with session expired error code
     */
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException(ErrorCode.SESSION_EXPIRED);
    }

    /**
     * Creates an AuthenticationException for unauthorized access.
     * 
     * @return AuthenticationException with unauthorized access error code
     */
    public static AuthenticationException unauthorizedAccess() {
        return new AuthenticationException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
}