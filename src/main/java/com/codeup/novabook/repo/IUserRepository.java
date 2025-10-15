package com.codeup.novabook.repo;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.domain.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * <p>
 * Extends the generic repository with User-specific query methods including
 * authentication, role-based queries, and email lookups.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>User CRUD operations</li>
 *   <li>Authentication support (findByEmail)</li>
 *   <li>User validation (email uniqueness)</li>
 *   <li>Role-based queries</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IUserRepository userRepo = new UserRepositoryImpl();
 * 
 * // Authentication
 * Optional<User> user = userRepo.findByEmail("admin@libronova.com");
 * 
 * // List all admins
 * List<User> admins = userRepo.findByRole(UserRole.ADMIN);
 * 
 * // Check email availability
 * boolean exists = userRepo.existsByEmail("john@example.com");
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see User
 * @see IGeneralRepository
 */
public interface IUserRepository extends IGeneralRepository<User, Integer> {
    
    /**
     * Finds a user by their email address.
     * <p>
     * Used primarily for authentication during login.
     * Email is unique in the system.
     * </p>
     * 
     * @param email the email address to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a user with the given email already exists.
     * <p>
     * Used for validation before creating new users.
     * </p>
     * 
     * @param email the email to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Finds all users with a specific role.
     * 
     * @param role the user role to filter by (ADMIN or ASSISTANT)
     * @return list of users with the specified role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Finds all users with a specific status.
     * 
     * @param status the user status to filter by (ACTIVE or INACTIVE)
     * @return list of users with the specified status
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * Updates a user's status.
     * <p>
     * Used for activating/deactivating user accounts.
     * </p>
     * 
     * @param userId the ID of the user to update
     * @param status the new status
     * @return true if updated successfully, false otherwise
     */
    boolean updateStatus(Integer userId, UserStatus status);
    
    /**
     * Updates only a user's password.
     * <p>
     * <b>IMPORTANT:</b> Password must be BCrypt-hashed BEFORE calling this method.
     * Use {@link com.codeup.novabook.util.PasswordUtils#hashPassword(String)} in the service layer.
     * </p>
     * 
     * @param userId the ID of the user to update
     * @param hashedPassword the BCrypt-hashed password
     * @return true if updated successfully, false otherwise
     */
    boolean updatePassword(Integer userId, String hashedPassword);
    
    /**
     * Updates only a user's email address.
     * <p>
     * Email must be validated before calling this method.
     * </p>
     * 
     * @param userId the ID of the user to update
     * @param newEmail the new email address
     * @return true if updated successfully, false otherwise
     */
    boolean updateEmail(Integer userId, String newEmail);
    
    /**
     * Updates only a user's phone number.
     * 
     * @param userId the ID of the user to update
     * @param newPhone the new phone number
     * @return true if updated successfully, false otherwise
     */
    boolean updatePhone(Integer userId, String newPhone);
}

