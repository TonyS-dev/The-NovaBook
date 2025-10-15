package com.codeup.novabook.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Member entity representing a library member in the LibroNova system.
 * <p>
 * This class encapsulates all member-related information including personal details,
 * contact information, and membership status. Members are the people who can
 * request book loans from the library.
 * </p>
 * 
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>Document ID must be unique across all members</li>
 *   <li>Email must be unique across all members</li>
 *   <li>Only ACTIVE members can request new loans</li>
 *   <li>SUSPENDED members cannot request loans until reactivated</li>
 *   <li>Default status is ACTIVE</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Member member = new Member();
 * member.setFirstName("John");
 * member.setLastName("Doe");
 * member.setDocumentId("12345678");
 * member.setEmail("john.doe@email.com");
 * member.setPhone("3001234567");
 * member.setStatus(MemberStatus.ACTIVE);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see MemberStatus
 * @see Loan
 */
public class Member {
    
    private Integer id;
    private String firstName;
    private String lastName;
    private String documentId; // National ID, passport, etc.
    private String email;
    private String phone;
    private String address;
    private MemberStatus status;
    private LocalDate registrationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for Member.
     */
    public Member() {
        this.status = MemberStatus.ACTIVE;
        this.registrationDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new Member with essential details.
     * 
     * @param firstName the first name of the member
     * @param lastName the last name of the member
     * @param documentId the unique document identifier (must be unique)
     * @param email the email address (must be unique)
     */
    public Member(String firstName, String lastName, String documentId, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentId = documentId;
        this.email = email;
    }

    /**
     * Gets the full name of the member (first name + last name).
     * 
     * @return the full name of the member
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Alias for getFullName() to support JavaFX PropertyValueFactory.
     * Used when PropertyValueFactory is configured with property name "name".
     * 
     * @return the full name of the member
     */
    public String getName() {
        return getFullName();
    }

    /**
     * Alias for getDocumentId() to support JavaFX PropertyValueFactory.
     * Used when PropertyValueFactory is configured with property name "documentNumber".
     * 
     * @return the document ID of the member
     */
    public String getDocumentNumber() {
        return documentId;
    }

    /**
     * Checks if this member is active and can request loans.
     * 
     * @return true if member is active, false otherwise
     */
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    /**
     * Checks if this member is suspended.
     * 
     * @return true if member is suspended, false otherwise
     */
    public boolean isSuspended() {
        return this.status == MemberStatus.SUSPENDED;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", registrationDate=" + registrationDate +
                '}';
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
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
