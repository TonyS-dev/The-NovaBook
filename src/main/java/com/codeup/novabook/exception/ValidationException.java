package com.codeup.novabook.exception;

/**
 * Exception thrown when validation fails for input data.
 * <p>
 * This exception is used throughout the NovaBook system to indicate that
 * user input or data does not meet the required validation criteria. It
 * extends {@link BusinessException} as validation failures are business
 * logic conditions that can be corrected by the user.
 * </p>
 * 
 * <p><strong>Common Validation Scenarios:</strong></p>
 * <ul>
 *   <li>Invalid email format</li>
 *   <li>Invalid phone number format</li>
 *   <li>Weak passwords</li>
 *   <li>Invalid name formats</li>
 *   <li>Invalid license plate formats</li>
 *   <li>Negative values where not allowed</li>
 *   <li>Missing required fields</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class ValidationException extends BusinessException {
    
    /**
     * Constructs a new ValidationException with the general invalid data error code.
     */
    public ValidationException() {
        super(ErrorCode.INVALID_DATA);
    }

    /**
     * Constructs a new ValidationException with a specific error code.
     * 
     * @param errorCode the specific validation error code
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new ValidationException with a specific error code and cause.
     * 
     * @param errorCode the specific validation error code
     * @param cause the underlying cause of this exception
     */
    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    // Convenience methods for common validation scenarios
    
    /**
     * Creates a ValidationException for invalid email format.
     * 
     * @return ValidationException with email format error code
     */
    public static ValidationException invalidEmail() {
        return new ValidationException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    /**
     * Creates a ValidationException for invalid phone format.
     * 
     * @return ValidationException with phone format error code
     */
    public static ValidationException invalidPhone() {
        return new ValidationException(ErrorCode.INVALID_PHONE_FORMAT);
    }

    /**
     * Creates a ValidationException for weak password.
     * 
     * @return ValidationException with weak password error code
     */
    public static ValidationException weakPassword() {
        return new ValidationException(ErrorCode.PASSWORD_TOO_WEAK);
    }

    /**
     * Creates a ValidationException for invalid name format.
     * 
     * @return ValidationException with name format error code
     */
    public static ValidationException invalidName() {
        return new ValidationException(ErrorCode.INVALID_NAME_FORMAT);
    }

    /**
     * Creates a ValidationException for missing required field.
     * 
     * @return ValidationException with missing field error code
     */
    public static ValidationException missingField() {
        return new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
    }
}