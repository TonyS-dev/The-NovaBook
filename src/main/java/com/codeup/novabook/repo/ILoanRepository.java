package com.codeup.novabook.repo;

import java.time.LocalDate;
import java.util.List;

import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.LoanStatus;

/**
 * Repository interface for Loan entity operations.
 * <p>
 * Extends the generic repository with Loan-specific query methods including
 * member/book filtering, status queries, overdue detection, and transaction support.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Loan CRUD operations</li>
 *   <li>Member and book loan history</li>
 *   <li>Active and overdue loan tracking</li>
 *   <li>Loan return processing</li>
 *   <li>CSV export support (overdue loans)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * ILoanRepository loanRepo = new LoanRepositoryImpl();
 * 
 * // Get member's active loans
 * List<Loan> activeLoans = loanRepo.findActiveLoansByMember(memberId);
 * 
 * // Get all overdue loans
 * List<Loan> overdueLoans = loanRepo.findOverdueLoans();
 * 
 * // Check if member has active loans
 * boolean hasActiveLoans = loanRepo.hasActiveLoansByMember(memberId);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Loan
 * @see IGeneralRepository
 */
public interface ILoanRepository extends IGeneralRepository<Loan, Integer> {
    
    /**
     * Finds all loans for a specific member.
     * 
     * @param memberId the ID of the member
     * @return list of all loans by the member
     */
    List<Loan> findByMemberId(Integer memberId);
    
    /**
     * Finds all loans for a specific book.
     * 
     * @param bookId the ID of the book
     * @return list of all loans of the book
     */
    List<Loan> findByBookId(Integer bookId);
    
    /**
     * Finds all loans with a specific status.
     * 
     * @param status the loan status to filter by
     * @return list of loans with the specified status
     */
    List<Loan> findByStatus(LoanStatus status);
    
    /**
     * Finds all active loans (not yet returned).
     * <p>
     * Shortcut for findByStatus(LoanStatus.ACTIVE).
     * </p>
     * 
     * @return list of active loans
     */
    List<Loan> findActiveLoans();
    
    /**
     * Finds all active loans for a specific member.
     * 
     * @param memberId the ID of the member
     * @return list of active loans by the member
     */
    List<Loan> findActiveLoansByMember(Integer memberId);
    
    /**
     * Finds all overdue loans (past expected return date and not returned).
     * <p>
     * Used for CSV export and overdue notifications.
     * </p>
     * 
     * @return list of overdue loans
     */
    List<Loan> findOverdueLoans();
    
    /**
     * Finds loans that are due within the specified number of days.
     * <p>
     * Used for upcoming due date reminders.
     * </p>
     * 
     * @param days number of days ahead to check
     * @return list of loans due within the specified period
     */
    List<Loan> findLoansDueWithinDays(int days);
    
    /**
     * Finds all loans within a date range.
     * 
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of loans within the date range
     */
    List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Checks if a member has any active loans.
     * <p>
     * Used for validation before suspending or deleting a member.
     * </p>
     * 
     * @param memberId the ID of the member
     * @return true if member has active loans, false otherwise
     */
    boolean hasActiveLoansByMember(Integer memberId);
    
    /**
     * Checks if a book is currently loaned (has active loans).
     * <p>
     * Used for validation before deactivating a book.
     * </p>
     * 
     * @param bookId the ID of the book
     * @return true if book has active loans, false otherwise
     */
    boolean hasActiveLoansByBook(Integer bookId);
    
    /**
     * Counts total loans by a member.
     * 
     * @param memberId the ID of the member
     * @return the total number of loans
     */
    int countLoansByMember(Integer memberId);
    
    /**
     * Counts how many times a book has been loaned.
     * 
     * @param bookId the ID of the book
     * @return the total number of times the book was loaned
     */
    int countLoansByBook(Integer bookId);
    
    /**
     * Gets the JdbcTemplateLight instance for transaction management.
     * <p>
     * This method exposes the JDBC template to allow service layer
     * to execute operations within transactions using txExecute().
     * </p>
     * 
     * @return the JdbcTemplateLight instance
     */
    com.codeup.novabook.jdbc.JdbcTemplateLight getJdbcTemplate();
}
