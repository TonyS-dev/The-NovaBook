package com.codeup.novabook.domain;

/**
 * Enumeration defining user account status in the LibroNova system.
 * <p>
 * This enum represents the different states a user account can be in,
 * determining whether they can access and use the system.
 * </p>
 * 
 * <p><b>Status Values:</b></p>
 * <ul>
 *   <li><b>ACTIVE</b> - User can log in and use the system normally (default)</li>
 *   <li><b>INACTIVE</b> - User account is temporarily disabled</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * User user = new User();
 * user.setStatus(UserStatus.ACTIVE);
 * 
 * if (user.getStatus() == UserStatus.ACTIVE) {
 *     // Allow login
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see User
 */
public enum UserStatus {
    /** Active user account - can access the system */
    ACTIVE,
    
    /** Inactive user account - temporarily disabled */
    INACTIVE,

    /** Deleted user account - account removed (soft delete) */
    DELETED
}
