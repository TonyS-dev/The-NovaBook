package com.codeup.novabook.domain;

import java.time.LocalDateTime;

/**
 * User entity representing a system user in the LibroNova application.
 * <p>
 * This class encapsulates all user-related information including personal details,
 * authentication credentials, role-based permissions (ADMIN/ASSISTANT), and account status.
 * Passwords must be stored in hashed format (BCrypt recommended) for security.
 * </p>
 * 
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>Email must be unique across the system</li>
 *   <li>Default role is ASSISTANT</li>
 *   <li>Default status is ACTIVE</li>
 *   <li>Password must be hashed before storage</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * User user = new User();
 * user.setName("John Doe");
 * user.setEmail("john@libronova.com");
 * user.setPassword(PasswordUtils.hashPassword("admin123"));
 * user.setRole(UserRole.ADMIN);
 * user.setStatus(UserStatus.ACTIVE);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see UserRole
 */
public class User {
    
    private Integer id;
    private String name;
    private String email;
    private String password; // BCrypt hashed
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for User.
     */
    public User() {
        this.role = UserRole.ASSISTANT;
        this.status = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new User with essential details.
     * <p>
     * Creates a user with basic information. Role defaults to ASSISTANT and status to ACTIVE.
     * </p>
     * 
     * @param name the full name of the user, must not be null or empty
     * @param email the email address, must be unique and valid format
     * @param password the hashed password for authentication (use BCrypt)
     * @param phone the phone number for contact purposes (optional)
     */
    public User(String name, String email, String password, String phone) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    /**
     * Checks if this user has ADMIN role.
     * 
     * @return true if user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * Checks if this user account is active.
     * 
     * @return true if user is active, false otherwise
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

