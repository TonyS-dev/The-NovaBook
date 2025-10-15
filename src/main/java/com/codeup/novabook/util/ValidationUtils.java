/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.codeup.novabook.util;

import java.util.regex.Pattern;

import com.codeup.novabook.exception.ErrorCode;
import com.codeup.novabook.exception.ValidationException;

/**
 * Comprehensive validation utility class for EcoFleet domain objects.
 * <p>
 * This class provides static validation methods for all major domain objects
 * in the EcoFleet system including users, vehicles, rentals, and their closure operations.
 * It enforces business rules and data integrity constraints using the standardized
 * {@link ErrorCode} system for consistent error handling.
 * </p>
 * 
 * <p><strong>Key validation capabilities:</strong></p>
 * <ul>
 * <li>User data validation (name, email, password, phone)</li>
 * <li>Vehicle data validation (brand, model, license plate, type)</li>
 * <li>Rental data validation (dates, IDs, costs, periods)</li>
 * <li>Individual field validation (email format, password strength)</li>
 * <li>Common validation helpers (empty checks, format validation)</li>
 * </ul>
 * 
 * <p><strong>Error Handling:</strong></p>
 * <p>All validation methods throw {@link ValidationException} with specific
 * {@link ErrorCode} values for different validation failures. This provides
 * consistent error handling and user-friendly error messages throughout the system.</p>
 * 
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * try {
 *     // Validate a complete user
 *     ValidationUtils.validateUser("John Doe", "john@email.com", "SecurePass123!", "1234567890");
 *     
 *     // Validate individual fields
 *     ValidationUtils.validateEmail("user@example.com");
 *     ValidationUtils.validatePassword("MyPassword123!");
 *     
 *     // Validate a rental
 *     ValidationUtils.validateRental(userId, vehicleId, startDate, endDate);
 * } catch (ValidationException e) {
 *     // Handle validation error with specific error code
 *     System.err.println("Validation failed: " + e.getCode() + " - " + e.getMessage());
 * }
 * }</pre>
 * 
 * <p>
 * This class follows the utility class pattern with a private constructor
 * to prevent instantiation and only static methods for functionality.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see com.codeup.novabook.exception.ValidationException
 * @see com.codeup.novabook.exception.ErrorCode
 */
public class ValidationUtils {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ValidationUtils() {}
    

    /**
     * Validates a person's name according to business rules.
     * <p>
     * Ensures the name is not empty and does not contain only digits.
     * Names should contain alphabetic characters and may include spaces.
     * </p>
     * 
     * @param name the name to validate
     * @throws ValidationException if the name is empty, null, or contains only digits
     */
    public static void validateName(String name) throws ValidationException {
        if (isEmptyOrNull(name)) {
            throw ValidationException.missingField();
        }
        if (isOnlyDigits(name)) {
            throw ValidationException.invalidName();
        }
    }

    /**
     * Validates an email address format using RFC 5322 simplified regex.
     * <p>
     * Ensures the email is not empty and matches a valid email format pattern.
     * The validation covers most common email formats but is simplified for practical use.
     * </p>
     * 
     * @param email the email address to validate
     * @throws ValidationException if the email is empty, null, or has invalid format
     */
    public static void validateEmail(String email) throws ValidationException {
        if (isEmptyOrNull(email)) throw ValidationException.missingField();
        // Regex for emails (RFC 5322 simplified)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (!pattern.matcher(email).matches()) {
            throw ValidationException.invalidEmail();
        }
    }

    /**
     * Validates password strength according to security requirements.
     * <p>
     * Enforces strong password policy requiring:
     * </p>
     * <ul>
     * <li>Minimum 8 characters length</li>
     * <li>At least one uppercase letter</li>
     * <li>At least one lowercase letter</li>
     * <li>At least one digit</li>
     * <li>At least one special character (!@#$%^&amp;+=)</li>
     * </ul>
     * 
     * @param password the password to validate
     * @throws ValidationException if the password doesn't meet strength requirements
     */
    public static void validatePassword(String password) throws ValidationException {
        if (isEmptyOrNull(password)) throw ValidationException.missingField();
        if (password.length() < 8) throw ValidationException.weakPassword();
        // Regex: at least 1 uppercase, 1 lowercase, 1 digit, 1 special
        String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$";
        Pattern pattern = Pattern.compile(passRegex);
        if (!pattern.matcher(password).matches()) {
            throw ValidationException.weakPassword();
        }
    }

    /**
     * Validates a phone number format according to business rules.
     * <p>
     * Ensures the phone number is not empty, contains only digits,
     * and has a length between 8 and 12 characters.
     * This validation supports international phone number formats.
     * </p>
     * 
     * @param phone the phone number to validate
     * @throws ValidationException if the phone is empty, null, contains non-digit characters, or length is not between 8-12 digits
     */
    public static void validatePhone(String phone) throws ValidationException {
        if (isEmptyOrNull(phone)) throw ValidationException.missingField();
        if (!isOnlyDigits(phone)) throw ValidationException.invalidPhone();
        if (phone.length() < 8 || phone.length() > 12) throw ValidationException.invalidPhone();
    }

    /**
     * Validates an age value ensuring it's a valid integer within reasonable bounds.
     * @param ageObj the age object to validate
     * @throws IllegalArgumentException if age is invalid
     */
    public static void validateAge(Object ageObj) {
        if (ageObj == null) throw new IllegalArgumentException("Age must be filled out.");
        int age;
        try {
            age = Integer.parseInt(ageObj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a number.");
        }
        if (isInRange(age, 1, 120)) {
            throw new IllegalArgumentException("Age must be between 1 and 120.");
        }
    }

    /**
     * Validates all user registration data in a single call.
     * <p>
     * This convenience method validates all user fields by calling individual
     * validation methods. It provides a comprehensive validation for user registration.
     * </p>
     * 
     * @param name the user's full name
     * @param email the user's email address
     * @param password the user's password
     * @param phone the user's phone number
     * @throws ValidationException if any field validation fails
     */
    public static void validateUser(String name, String email, String password, String phone) throws ValidationException {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validatePhone(phone);
    }

    /**
     * Validates book data according to business rules.
     * <p>
     * Ensures all required book fields are present and valid.
     * ISBN, title, author, and category are mandatory fields.
     * </p>
     * 
     * @param isbn the book ISBN (unique identifier)
     * @param title the book title
     * @param author the book author
     * @param category the book category/genre
     * @throws ValidationException if any book field validation fails
     */
    public static void validateBook(String isbn, String title, String author, String category) throws ValidationException {
        if (isEmptyOrNull(isbn) || isEmptyOrNull(title) || isEmptyOrNull(author) || isEmptyOrNull(category)) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        // Validate ISBN format (basic check: 10 or 13 digits with optional hyphens)
        String isbnClean = isbn.replaceAll("-", "");
        if (!isOnlyDigits(isbnClean) || (isbnClean.length() != 10 && isbnClean.length() != 13)) {
            throw new ValidationException(ErrorCode.INVALID_ISBN_FORMAT);
        }
    }
    
    /**
     * Validates member registration data according to business rules.
     * <p>
     * Ensures all required member fields are present and valid.
     * Document ID, name, email, and phone are mandatory fields.
     * </p>
     * 
     * @param documentId the member's document/ID number
     * @param name the member's full name
     * @param email the member's email address
     * @param phone the member's phone number
     * @throws ValidationException if any member field validation fails
     */
    public static void validateMember(String documentId, String name, String email, String phone) throws ValidationException {
        if (isEmptyOrNull(documentId)) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        // Validate document ID format (alphanumeric)
        if (!documentId.matches("^[a-zA-Z0-9]+$")) {
            throw new ValidationException(ErrorCode.INVALID_DOCUMENT_ID);
        }
        
        // Reuse existing user validation for name, email, phone
        validateName(name);
        validateEmail(email);
        validatePhone(phone);
    }
    
    /**
     * Validates loan data according to business rules.
     * <p>
     * Ensures all required loan fields are present and valid.
     * Member ID, book ID, and loan date must be provided.
     * </p>
     * 
     * @param memberId the ID of the member borrowing the book
     * @param bookId the ID of the book being borrowed
     * @param loanDate the date when the loan is created
     * @throws ValidationException if any loan field validation fails
     */
    public static void validateLoan(Integer memberId, Integer bookId, java.time.LocalDate loanDate) throws ValidationException {
        if (isEmptyOrNull(memberId) || isEmptyOrNull(bookId) || isEmptyOrNull(loanDate)) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        // Loan date cannot be in the future
        if (loanDate.isAfter(java.time.LocalDate.now())) {
            throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
    
    /**
     * Validates loan return/closure data according to business rules.
     * <p>
     * Ensures all required closure fields are present and values are valid.
     * Return date must be after loan date, and fine amount must be non-negative.
     * </p>
     * 
     * @param loanDate the original loan date
     * @param returnDate the date when the book is returned
     * @param fineAmount the fine amount (if any)
     * @throws ValidationException if any closure field validation fails
     */
    public static void validateLoanClosure(java.time.LocalDate loanDate, java.time.LocalDate returnDate, Double fineAmount) throws ValidationException {
        if (isEmptyOrNull(returnDate)) {
            throw new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        // Return date must be after or equal to loan date
        if (returnDate.isBefore(loanDate)) {
            throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
        }
        
        // Fine amount cannot be negative
        if (fineAmount != null && fineAmount < 0) {
            throw new ValidationException(ErrorCode.NEGATIVE_VALUE_NOT_ALLOWED);
        }
    }

    /**
     * Checks if a string is null or empty.
     * @param str the string to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmptyOrNull(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if an object is null or its string representation is empty.
     * @param obj the object to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmptyOrNull(Object obj) {
        return obj == null || obj.toString().isEmpty();
    }

    /**
     * Checks if a string contains only letters.
     * @param str the string to check
     * @return true if contains only letters, false otherwise
     */
    public static boolean isOnlyLetters(String str) {
        return str.chars().allMatch(Character::isLetter);
    }

    /**
     * Checks if a string contains only digits.
     * @param str the string to check
     * @return true if contains only digits, false otherwise
     */
    public static boolean isOnlyDigits(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

    /**
     * Checks if a number is outside the specified range.
     * @param number the number to check
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return true if outside range, false if within range
     */
    public static boolean isInRange(int number, int min, int max) {
        return (number < min || number > max);
    }
}