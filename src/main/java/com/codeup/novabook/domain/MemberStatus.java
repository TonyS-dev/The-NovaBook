package com.codeup.novabook.domain;

/**
 * Enumeration defining member account status in the LibroNova system.
 * <p>
 * This enum represents the different states a library member can be in,
 * determining whether they can request new book loans.
 * </p>
 * 
 * <p><b>Status Values:</b></p>
 * <ul>
 *   <li><b>ACTIVE</b> - Member can request loans normally (default)</li>
 *   <li><b>INACTIVE</b> - Member temporarily cannot request loans</li>
 *   <li><b>SUSPENDED</b> - Member suspended (e.g., overdue fines, violations)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Member member = new Member();
 * member.setStatus(MemberStatus.ACTIVE);
 * 
 * if (member.getStatus() == MemberStatus.SUSPENDED) {
 *     // Deny loan request
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Member
 */
public enum MemberStatus {
    /** Active member - can request loans */
    ACTIVE,
    
    /** Inactive member - temporarily cannot request loans */
    INACTIVE,
    
    /** Suspended member - blocked from requesting loans */
    SUSPENDED,

    /** Deleted member - account removed (soft delete) */
    DELETED
}
