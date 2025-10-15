package com.codeup.novabook.repo;

import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Member entity operations.
 * <p>
 * Extends the generic repository with Member-specific query methods including
 * document ID lookups, status filtering, and member validation.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Member CRUD operations</li>
 *   <li>Document ID and email uniqueness validation</li>
 *   <li>Status-based member queries</li>
 *   <li>Active member filtering for loan operations</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IMemberRepository memberRepo = new MemberRepositoryImpl();
 * 
 * // Find member by document ID
 * Optional<Member> member = memberRepo.findByDocumentId("12345678");
 * 
 * // Get all active members
 * List<Member> activeMembers = memberRepo.findByStatus(MemberStatus.ACTIVE);
 * 
 * // Suspend a member
 * memberRepo.updateStatus(memberId, MemberStatus.SUSPENDED);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Member
 * @see IGeneralRepository
 */
public interface IMemberRepository extends IGeneralRepository<Member, Integer> {
    
    /**
     * Finds a member by their document ID.
     * <p>
     * Document ID is unique in the system (national ID, passport, etc.).
     * </p>
     * 
     * @param documentId the document ID to search for
     * @return an Optional containing the member if found, empty otherwise
     */
    Optional<Member> findByDocumentId(String documentId);
    
    /**
     * Finds a member by their email address.
     * <p>
     * Email is unique in the system.
     * </p>
     * 
     * @param email the email address to search for
     * @return an Optional containing the member if found, empty otherwise
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Checks if a member with the given document ID already exists.
     * 
     * @param documentId the document ID to check
     * @return true if a member with this document ID exists, false otherwise
     */
    boolean existsByDocumentId(String documentId);
    
    /**
     * Checks if a member with the given email already exists.
     * 
     * @param email the email to check
     * @return true if a member with this email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Finds all members with a specific status.
     * 
     * @param status the member status to filter by
     * @return list of members with the specified status
     */
    List<Member> findByStatus(MemberStatus status);
    
    /**
     * Finds all active members who can request loans.
     * <p>
     * Shortcut method for findByStatus(MemberStatus.ACTIVE).
     * </p>
     * 
     * @return list of active members
     */
    List<Member> findActiveMembers();
    
    /**
     * Updates a member's status.
     * <p>
     * Used for activating/deactivating/suspending member accounts.
     * </p>
     * 
     * @param memberId the ID of the member to update
     * @param status the new status
     * @return true if updated successfully, false otherwise
     */
    boolean updateStatus(Integer memberId, MemberStatus status);
    
    /**
     * Updates only a member's email address.
     * 
     * @param memberId the ID of the member to update
     * @param newEmail the new email address
     * @return true if updated successfully, false otherwise
     */
    boolean updateEmail(Integer memberId, String newEmail);
    
    /**
     * Updates only a member's phone number.
     * 
     * @param memberId the ID of the member to update
     * @param newPhone the new phone number
     * @return true if updated successfully, false otherwise
     */
    boolean updatePhone(Integer memberId, String newPhone);
    
    /**
     * Updates only a member's address.
     * 
     * @param memberId the ID of the member to update
     * @param newAddress the new address
     * @return true if updated successfully, false otherwise
     */
    boolean updateAddress(Integer memberId, String newAddress);
    
    /**
     * Searches members by name (first or last).
     * <p>
     * Case-insensitive partial match search.
     * </p>
     * 
     * @param searchTerm the name to search for
     * @return list of members matching the search criteria
     */
    List<Member> searchByName(String searchTerm);
}
