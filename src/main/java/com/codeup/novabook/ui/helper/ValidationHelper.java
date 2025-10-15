package com.codeup.novabook.ui.helper;

import java.util.regex.Pattern;

/**
 * Utility class for common UI input validation.
 * Provides reusable validation methods with clear error messages.
 */
public class ValidationHelper {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,12}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^[0-9\\-]+$");
    
    private ValidationHelper() {
        // Utility class, no instantiation
    }
    
    /**
     * Validates that a string is not null or empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates phone number format (8-12 digits)
     */
    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates ISBN format (numbers and hyphens only)
     */
    public static boolean isValidIsbn(String isbn) {
        return isNotEmpty(isbn) && ISBN_PATTERN.matcher(isbn).matches();
    }
    
    /**
     * Validates password strength (min 8 characters)
     */
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 8;
    }
    
    /**
     * Validates that a number is positive
     */
    public static boolean isPositive(Integer number) {
        return number != null && number > 0;
    }
    
    /**
     * Validates that a number is non-negative
     */
    public static boolean isNonNegative(Integer number) {
        return number != null && number >= 0;
    }
    
    /**
     * Gets a user-friendly error message for empty field
     */
    public static String getEmptyFieldMessage(String fieldName) {
        return fieldName + " is required";
    }
    
    /**
     * Gets a user-friendly error message for invalid email
     */
    public static String getInvalidEmailMessage() {
        return "Invalid email format. Example: user@mail.com";
    }
    
    /**
     * Gets a user-friendly error message for invalid phone
     */
    public static String getInvalidPhoneMessage() {
        return "Invalid phone number. Must be 8-12 digits";
    }
    
    /**
     * Gets a user-friendly error message for invalid ISBN
     */
    public static String getInvalidIsbnMessage() {
        return "Invalid ISBN format. Must contain only numbers and hyphens";
    }
    
    /**
     * Gets a user-friendly error message for weak password
     */
    public static String getWeakPasswordMessage() {
        return "Password must be at least 8 characters long";
    }
}
