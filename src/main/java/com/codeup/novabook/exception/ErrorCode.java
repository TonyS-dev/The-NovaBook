package com.codeup.novabook.exception;

/**
 * Centralized error code definitions for the NovaBook library management system.
 * <p>
 * This enum provides a standardized catalog of all possible errors in the system,
 * including error codes, human-readable messages, and severity levels. This approach
 * enables consistent error handling, better logging, internationalization support,
 * and improved maintainability.
 * </p>
 * 
 * <p><strong>Error Code Categories:</strong></p>
 * <ul>
 *   <li><strong>E001-E049:</strong> User Management Errors</li>
 *   <li><strong>E050-E099:</strong> Member Management Errors</li>
 *   <li><strong>E100-E149:</strong> Book Management Errors</li>
 *   <li><strong>E150-E199:</strong> Loan Management Errors</li>
 *   <li><strong>E400-E499:</strong> Authentication &amp; Authorization Errors</li>
 *   <li><strong>E500-E599:</strong> Validation Errors</li>
 *   <li><strong>E600-E699:</strong> Database &amp; Infrastructure Errors</li>
 *   <li><strong>E900-E999:</strong> System &amp; Unknown Errors</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
public enum ErrorCode {

    // --- User Management Errors (E001-E049) ---
    /**
     * User not found error (E001).
     */
    USER_NOT_FOUND("E001", "User not found", Severity.WARN),
    /**
     * User already exists error (E002).
     */
    USER_ALREADY_EXISTS("E002", "User already exists with this email", Severity.WARN),
    /**
     * User account is inactive error (E003).
     */
    USER_INACTIVE("E003", "User account is inactive", Severity.WARN),
    /**
     * User account has been deleted error (E004).
     */
    USER_DELETED("E004", "User account has been deleted", Severity.WARN),

    // --- Member Management Errors (E050-E099) ---
    /**
     * Member not found error (E050).
     */
    MEMBER_NOT_FOUND("E050", "Member not found", Severity.WARN),
    /**
     * Member already exists error (E051).
     */
    MEMBER_ALREADY_EXISTS("E051", "Member already exists with this document ID or email", Severity.WARN),
    /**
     * Member account is inactive error (E052).
     */
    MEMBER_INACTIVE("E052", "Member account is inactive", Severity.WARN),
    /**
     * Member is suspended error (E053).
     */
    MEMBER_SUSPENDED("E053", "Member is suspended", Severity.WARN),
    /**
     * Member has been deleted error (E054).
     */
    MEMBER_DELETED("E054", "Member has been deleted", Severity.WARN),

    // --- Book Management Errors (E100-E149) ---
    /**
     * Book not found error (E100).
     */
    BOOK_NOT_FOUND("E100", "Book not found", Severity.WARN),
    /**
     * Book not available for loan error (E101).
     */
    BOOK_NOT_AVAILABLE("E101", "Book is not available for loan", Severity.WARN),
    /**
     * Book stock is insufficient error (E102).
     */
    BOOK_INSUFFICIENT_STOCK("E102", "Book stock is insufficient", Severity.WARN),
    /**
     * Book already exists error (E103).
     */
    BOOK_ALREADY_EXISTS("E103", "Book already exists with this ISBN", Severity.WARN),

    // --- Loan Management Errors (E150-E199) ---
    /**
     * Loan not found error (E150).
     */
    LOAN_NOT_FOUND("E150", "Loan not found", Severity.WARN),
    /**
     * Loan already closed error (E151).
     */
    LOAN_ALREADY_CLOSED("E151", "Loan is already closed", Severity.WARN),
    /**
     * Loan is overdue error (E152).
     */
    LOAN_OVERDUE("E152", "Loan is overdue", Severity.WARN),
    /**
     * Member has reached maximum active loans error (E153).
     */
    MAX_ACTIVE_LOANS_REACHED("E153", "Member has reached maximum active loans", Severity.WARN),
    /**
     * Member already has an active loan of this book error (E154).
     */
    DUPLICATE_BOOK_LOAN("E154", "Member already has an active loan of this book", Severity.WARN),

    // --- Authentication & Authorization Errors (E400-E499) ---
    /**
     * Authentication failed error (E400).
     */
    AUTHENTICATION_FAILED("E400", "Invalid email or password", Severity.WARN),
    /**
     * Insufficient permissions to perform this action error (E401).
     */
    UNAUTHORIZED_ACCESS("E401", "Insufficient permissions to perform this action", Severity.WARN),
    /**
     * User session has expired error (E402).
     */
    SESSION_EXPIRED("E402", "User session has expired", Severity.WARN),
    /**
     * Invalid authentication credentials error (E403).
     */
    INVALID_CREDENTIALS("E403", "Invalid authentication credentials", Severity.WARN),

    // --- Validation Errors (E500-E599) ---
    /**
     * Invalid or incomplete data provided error (E500).
     */
    INVALID_DATA("E500", "Invalid or incomplete data provided", Severity.WARN),
    /**
     * Invalid email format error (E501).
     */
    INVALID_EMAIL_FORMAT("E501", "Invalid email format", Severity.WARN),
    /**
     * Invalid phone number format error (E502).
     */
    INVALID_PHONE_FORMAT("E502", "Invalid phone number format", Severity.WARN),
    /**
     * Password does not meet security requirements error (E503).
     */
    PASSWORD_TOO_WEAK("E503", "Password does not meet security requirements", Severity.WARN),
    /**
     * Invalid name format error (E504).
     */
    INVALID_NAME_FORMAT("E504", "Invalid name format - only letters and spaces allowed", Severity.WARN),
    /**
     * Invalid ISBN format error (E505).
     */
    INVALID_ISBN_FORMAT("E505", "Invalid ISBN format", Severity.WARN),
    /**
     * Negative values are not allowed error (E506).
     */
    NEGATIVE_VALUE_NOT_ALLOWED("E506", "Negative values are not allowed", Severity.WARN),
    /**
     * Required field is missing error (E507).
     */
    REQUIRED_FIELD_MISSING("E507", "Required field is missing", Severity.WARN),
    /**
     * Invalid date range specified error (E508).
     */
    INVALID_DATE_RANGE("E508", "Invalid date range specified", Severity.WARN),
    /**
     * Invalid document ID format error (E509).
     */
    INVALID_DOCUMENT_ID("E509", "Invalid document ID format", Severity.WARN),

    // --- Database & Infrastructure Errors (E600-E699) ---
    /**
     * Database operation failed error (E600).
     */
    DATABASE_ERROR("E600", "Database operation failed", Severity.ERROR),
    /**
     * Failed to connect to database error (E601).
     */
    DATABASE_CONNECTION_FAILED("E601", "Failed to connect to database", Severity.FATAL),
    /**
     * Database transaction failed error (E602).
     */
    TRANSACTION_FAILED("E602", "Database transaction failed", Severity.ERROR),
    /**
     * Data integrity constraint violation error (E603).
     */
    DATA_INTEGRITY_VIOLATION("E603", "Data integrity constraint violation", Severity.ERROR),
    /**
     * Resource was modified by another process error (E604).
     */
    CONCURRENT_MODIFICATION("E604", "Resource was modified by another process", Severity.WARN),
    /**
     * Resource is currently locked by another operation error (E605).
     */
    RESOURCE_LOCKED("E605", "Resource is currently locked by another operation", Severity.WARN),

    // --- System & Unknown Errors (E900-E999) ---
    /**
     * Internal system error occurred (E900).
     */
    SYSTEM_ERROR("E900", "Internal system error occurred", Severity.ERROR),
    /**
     * System configuration error (E901).
     */
    CONFIGURATION_ERROR("E901", "System configuration error", Severity.FATAL),
    /**
     * Service is temporarily unavailable error (E902).
     */
    SERVICE_UNAVAILABLE("E902", "Service is temporarily unavailable", Severity.ERROR),
    /**
     * An unknown error occurred (E999).
     */
    UNKNOWN_ERROR("E999", "An unknown error occurred", Severity.FATAL);

    private final String code;
    private final String message;
    private final Severity severity;

    /**
     * Constructor for ErrorCode enum values.
     * 
     * @param code the unique error code identifier
     * @param message the human-readable error message
     * @param severity the severity level of the error
     */
    ErrorCode(String code, String message, Severity severity) {
        this.code = code;
        this.message = message;
        this.severity = severity;
    }

    /**
     * Gets the unique error code identifier.
     * 
     * @return the error code
     */
    public String getCode() { 
        return code; 
    }

    /**
     * Gets the human-readable error message.
     * 
     * @return the error message
     */
    public String getMessage() { 
        return message; 
    }

    /**
     * Gets the severity level of the error.
     * 
     * @return the severity level
     */
    public Severity getSeverity() { 
        return severity; 
    }

    /**
     * Returns a formatted string representation of the error code.
     * 
     * @return formatted error information
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", severity, code, message);
    }
}