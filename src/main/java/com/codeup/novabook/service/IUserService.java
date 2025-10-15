package com.codeup.novabook.service;

import java.util.List;
import java.util.Optional;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.exception.AuthenticationException;
import com.codeup.novabook.exception.UserAlreadyExistsException;
import com.codeup.novabook.exception.UserNotFoundException;
import com.codeup.novabook.exception.ValidationException;

/**
 * Service interface for managing user operations in the NovaBook system.
 * <p>
 * This interface defines the contract for comprehensive user management functionality 
 * including registration, authentication, profile updates, and administrative operations.
 * Implementations must enforce business rules and validate data before delegating 
 * to the repository layer.
 * </p>
 * 
 * <p><strong>Key responsibilities:</strong></p>
 * <ul>
 *   <li>User registration with validation and password hashing</li>
 *   <li>Secure authentication with credential verification</li>
 *   <li>Profile management and updates</li>
 *   <li>Administrative user creation with roles and permissions</li>
 *   <li>User existence and duplicate detection</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * IUserService userService = new UserService(userRepository);
 * 
 * // Register a new user
 * userService.register("John Doe", "john@email.com", "SecurePass123!", "1234567890");
 * 
 * // Authenticate user
 * Optional<User> user = userService.authenticate("john@email.com", "SecurePass123!");
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 2.0
 * @see com.codeup.novabook.domain.User
 * @see com.codeup.novabook.exception.ValidationException
 * @see com.codeup.novabook.exception.AuthenticationException
 */
public interface IUserService {

    /**
     * Registers a new user in the system.
     * <p>
     * This method validates the input data, ensures the email is unique,
     * and creates a new user account with default permissions.
     * The password is automatically hashed before storage.
     * </p>
     * 
     * @param name the full name of the user, must not be empty or contain only digits
     * @param email the email address, must be valid format and unique
     * @param password the plain text password, must meet complexity requirements
     * @param phone the phone number, must contain only digits
     * @throws ValidationException if any input validation fails
     * @throws UserAlreadyExistsException if a user with the email already exists
     */
    void register(String name, String email, String password, String phone) 
            throws ValidationException, UserAlreadyExistsException;

    /**
     * Registers a new user with administrative privileges.
     * <p>
     * This method is used by administrators to create user accounts with specific
     * roles and access levels. It performs the same validation as regular registration
     * but allows setting administrative properties.
     * </p>
     * 
     * @param name the full name of the user
     * @param email the email address, must be unique
     * @param password the plain text password
     * @param phone the phone number
     * @param role the user role (USER, ADMIN)
     * @param accessLevel the access level (READ_ONLY, READ_WRITE, MANAGE)
     * @throws ValidationException if any input validation fails
     * @throws UserAlreadyExistsException if a user with the email already exists
     * @throws UserNotFoundException if the created user cannot be found for role assignment
     */
    void adminRegister(String name, String email, String password, String phone, String role, String accessLevel) 
            throws ValidationException, UserAlreadyExistsException, UserNotFoundException;

    /**
     * Changes the name of an existing user.
     * 
     * @param user the user to rename
     * @param newName the new name for the user
     */
    void rename(User user, String newName);

    /**
     * Changes the password of an existing user.
     * <p>
     * The new password is automatically hashed before storage.
     * </p>
     * 
     * @param user the user whose password will be changed
     * @param newPassword the new plain text password
     */
    void changePassword(User user, String newPassword);

    /**
     * Updates user information identified by email.
     * 
     * @param user the user object with updated information
     * @param email the email address to identify the user to update
     */
    void updateUserByEmail(User user, String email);

    /**
     * Finds an active user by email address.
     * 
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all users in the system.
     * 
     * @return a list of all users, may be empty but never null
     */
    List<User> list();

    /**
     * Checks if a user exists with the given email address.
     * 
     * @param email the email address to check
     * @return {@code true} if a user exists with the email, {@code false} otherwise
     */
    boolean userExists(String email);

    /**
     * Authenticates a user with email and password credentials.
     * <p>
     * This method verifies the provided credentials against stored user data.
     * It only considers active, non-deleted users for authentication.
     * Password verification uses BCrypt hashing for security.
     * </p>
     * 
     * @param email the user's email address
     * @param password the plain text password to verify
     * @return an {@link Optional} containing the authenticated user if successful
     * @throws AuthenticationException if the credentials are invalid or user not found
     */
    Optional<User> authenticate(String email, String password) throws AuthenticationException;
}