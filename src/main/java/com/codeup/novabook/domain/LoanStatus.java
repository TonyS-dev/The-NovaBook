package com.codeup.novabook.domain;

/**
 * Enumeration defining loan status in the LibroNova system.
 * <p>
 * This enum represents the different states a book loan can be in during its lifecycle.
 * </p>
 * 
 * <p><b>Status Values:</b></p>
 * <ul>
 *   <li><b>ACTIVE</b> - Loan is currently active, book not yet returned (default)</li>
 *   <li><b>RETURNED</b> - Book returned on time, no fines</li>
 *   <li><b>OVERDUE</b> - Book returned late, fines applied</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Loan loan = new Loan();
 * loan.setStatus(LoanStatus.ACTIVE);
 * 
 * // When book is returned
 * if (loan.getDaysOverdue() > 0) {
 *     loan.setStatus(LoanStatus.OVERDUE);
 * } else {
 *     loan.setStatus(LoanStatus.RETURNED);
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Loan
 */
public enum LoanStatus {
    /** Loan is active - book not yet returned */
    ACTIVE,
    
    /** Book returned on time - no penalty */
    RETURNED,
    
    /** Book returned late - fine calculated */
    OVERDUE
}
