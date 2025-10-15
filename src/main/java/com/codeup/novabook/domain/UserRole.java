package com.codeup.novabook.domain;

/**
 * Enumeration defining user roles in the NovaBook library system.
 * <p>
 * This enum represents the two levels of system access for staff users.
 * The role determines what operations a user can perform within the application.
 * </p>
 * 
 * <p><b>Important:</b> This enum is ONLY for system users (staff). Library members
 * are managed in a separate {@code members} table and do NOT have system access.</p>
 * 
 * <p><b>Roles:</b></p>
 * <ul>
 *   <li><b>ADMIN</b> - Full system access: manage users, books, members, loans, reports, and configuration</li>
 *   <li><b>ASSISTANT</b> - Limited staff access: manage members and loans, view catalog (can register books but not deactivate)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * User adminUser = new User();
 * adminUser.setRole(UserRole.ADMIN);
 * 
 * User assistantUser = new User();
 * assistantUser.setRole(UserRole.ASSISTANT); // Default for new registrations
 * 
 * if (user.getRole() == UserRole.ADMIN) {
 *     // Grant full administrative privileges
 * } else if (user.getRole() == UserRole.ASSISTANT) {
 *     // Grant limited staff privileges
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see User
 */
public enum UserRole {
    /** Administrative user with full system management capabilities */
    ADMIN,
    
    /** Library assistant/staff with operational privileges (default for registration) */
    ASSISTANT
}

