/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.codeup.novabook.util;

import org.mindrot.jbcrypt.BCrypt;
/**
 * Utility class for secure password operations using BCrypt hashing.
 * <p>
 * This class provides static methods for password hashing and verification
 * using the BCrypt algorithm, which is considered secure for password storage.
 * BCrypt automatically handles salt generation and is resistant to rainbow table attacks.
 * </p>
 * <p>Example usage:</p>
 * <pre>{@code
 * // Hash a password for storage
 * String plainPassword = "MySecurePassword123!";
 * String hashedPassword = PasswordUtils.hashPassword(plainPassword);
 * 
 * // Verify a password during authentication
 * boolean isValid = PasswordUtils.checkPassword(plainPassword, hashedPassword);
 * }</pre>
 * 
 * <p>
 * This class follows the utility class pattern with a private constructor
 * to prevent instantiation and only static methods for functionality.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see org.mindrot.jbcrypt.BCrypt
 */
public class PasswordUtils {
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PasswordUtils() {}
    
    /**
     * Hashes a plain text password using BCrypt with automatic salt generation.
     * <p>
     * This method generates a random salt and hashes the password using BCrypt
     * algorithm. The resulting hash includes the salt and can be safely stored
     * in the database.
     * </p>
     * 
     * @param plainPassword the plain text password to hash, must not be null
     * @return the BCrypt hashed password including salt
     * @throws IllegalArgumentException if plainPassword is null
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    /**
     * Verifies a plain text password against a BCrypt hash.
     * <p>
     * This method checks if the provided plain text password matches
     * the stored BCrypt hash. It automatically extracts the salt from
     * the hash and performs the verification.
     * </p>
     * 
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the BCrypt hash to verify against
     * @return {@code true} if the password matches the hash, {@code false} otherwise
     * @throws IllegalArgumentException if either parameter is null
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
