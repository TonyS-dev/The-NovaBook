package com.codeup.novabook.service;

import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.exception.MemberAlreadyExistsException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;

import java.util.List;

/**
 * Service interface for Member business logic.
 * <p>
 * This interface defines business operations for library member management including
 * registration, profile updates, status management, and validation rules.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Member registration and validation</li>
 *   <li>Document ID and email uniqueness checks</li>
 *   <li>Status management (ACTIVE, INACTIVE, SUSPENDED)</li>
 *   <li>Loan eligibility validation</li>
 *   <li>Member search and filtering</li>
 * </ul>
 * 
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>Document ID must be unique</li>
 *   <li>Email must be unique</li>
 *   <li>Only ACTIVE members can request loans</li>
 *   <li>SUSPENDED members have overdue loans or unpaid fines</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Member
 * @see com.codeup.novabook.repo.IMemberRepository
 */
public interface IMemberService {
    
    /**
     * Registers a new library member.
     * <p>
     * Default status: ACTIVE
     * Registration date: current date
     * </p>
     * 
     * @param firstName member's first name
     * @param lastName member's last name
     * @param documentId national ID or passport (must be unique)
     * @param email email address (must be unique)
     * @param phone phone number
     * @param address home address
     * @return created Member object with ID
     * @throws MemberAlreadyExistsException if documentId or email already exists
     * @throws ValidationException if any field is invalid
     */
    Member registerMember(String firstName, String lastName, String documentId, 
                         String email, String phone, String address) 
            throws MemberAlreadyExistsException, ValidationException;
    
    /**
     * Finds a member by ID.
     * 
     * @param memberId the member ID
     * @return Member object
     * @throws MemberNotFoundException if member not found
     */
    Member getMemberById(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Finds a member by document ID.
     * 
     * @param documentId the document ID
     * @return Member object
     * @throws MemberNotFoundException if member not found
     */
    Member getMemberByDocumentId(String documentId) throws MemberNotFoundException;
    
    /**
     * Finds a member by email.
     * 
     * @param email the email address
     * @return Member object
     * @throws MemberNotFoundException if member not found
     */
    Member getMemberByEmail(String email) throws MemberNotFoundException;
    
    /**
     * Lists all members in the system.
     * 
     * @return list of all members (including deleted)
     */
    List<Member> getAllMembers();
    
    /**
     * Lists only active members who can request loans.
     * 
     * @return list of active members
     */
    List<Member> getActiveMembers();
    
    /**
     * Lists members by status.
     * 
     * @param status the status to filter by
     * @return list of members with specified status
     */
    List<Member> getMembersByStatus(MemberStatus status);
    
    /**
     * Searches members by name (first or last).
     * <p>
     * Case-insensitive partial match.
     * </p>
     * 
     * @param searchTerm the name to search for
     * @return list of matching members
     */
    List<Member> searchMembersByName(String searchTerm);
    
    /**
     * Updates member profile information.
     * 
     * @param memberId the member ID
     * @param firstName new first name
     * @param lastName new last name
     * @param phone new phone number
     * @param address new address
     * @return updated Member object
     * @throws MemberNotFoundException if member not found
     * @throws ValidationException if any field is invalid
     */
    Member updateMemberProfile(Integer memberId, String firstName, String lastName, 
                              String phone, String address) 
            throws MemberNotFoundException, ValidationException;
    
    /**
     * Updates member email.
     * 
     * @param memberId the member ID
     * @param newEmail the new email (must be unique)
     * @return updated Member object
     * @throws MemberNotFoundException if member not found
     * @throws MemberAlreadyExistsException if email already exists
     * @throws ValidationException if email format is invalid
     */
    Member updateMemberEmail(Integer memberId, String newEmail) 
            throws MemberNotFoundException, MemberAlreadyExistsException, ValidationException;
    
    /**
     * Activates a member account.
     * 
     * @param memberId the member ID
     * @return updated Member object
     * @throws MemberNotFoundException if member not found
     */
    Member activateMember(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Deactivates a member account temporarily.
     * 
     * @param memberId the member ID
     * @return updated Member object
     * @throws MemberNotFoundException if member not found
     */
    Member deactivateMember(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Suspends a member (e.g., for overdue loans or unpaid fines).
     * 
     * @param memberId the member ID
     * @return updated Member object
     * @throws MemberNotFoundException if member not found
     */
    Member suspendMember(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Soft deletes a member.
     * <p>
     * Sets status to DELETED but keeps record in database.
     * </p>
     * 
     * @param memberId the member ID
     * @throws MemberNotFoundException if member not found
     */
    void deleteMember(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Checks if member can request new loans.
     * <p>
     * Member must be ACTIVE (not INACTIVE, SUSPENDED, or DELETED).
     * </p>
     * 
     * @param memberId the member ID
     * @return true if eligible, false otherwise
     * @throws MemberNotFoundException if member not found
     */
    boolean isEligibleForLoan(Integer memberId) throws MemberNotFoundException;
    
    /**
     * Checks if document ID already exists.
     * 
     * @param documentId the document ID to check
     * @return true if exists, false otherwise
     */
    boolean documentIdExists(String documentId);
    
    /**
     * Checks if email already exists.
     * 
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    boolean emailExists(String email);
}
