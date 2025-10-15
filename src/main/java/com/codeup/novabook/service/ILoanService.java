package com.codeup.novabook.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.exception.BookNotAvailableException;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.LoanAlreadyClosedException;
import com.codeup.novabook.exception.LoanNotFoundException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;

/**
 * Service interface for Loan management operations.
 * <p>
 * This interface defines all business logic operations related to book loans,
 * including transaction management, fine calculations, overdue tracking, and
 * loan lifecycle management (create, return, extend).
 * </p>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>TRANSACTION MANAGEMENT:</strong> Create loan with atomic operations 
 *       (validate member + check book + decrease stock + create loan record)</li>
 *   <li><strong>FINE CALCULATION:</strong> Calculate overdue fines based on days late</li>
 *   <li><strong>OVERDUE TRACKING:</strong> Identify and report overdue loans for CSV export</li>
 *   <li><strong>LOAN LIFECYCLE:</strong> Create, return, extend, close loans</li>
 *   <li><strong>BUSINESS RULES:</strong> Maximum active loans, loan eligibility, stock validation</li>
 * </ul>
 * 
 * <p><strong>Transaction Example (LibroNova Statement Requirement):</strong></p>
 * <pre>{@code
 * // In implementation:
 * Connection conn = ConnectionFactory.open();
 * try {
 *     conn.setAutoCommit(false);  // Start transaction
 *     
 *     // 1. Validate member is active
 *     // 2. Check book is available and has stock
 *     // 3. Decrease book stock
 *     // 4. Create loan record
 *     
 *     conn.commit();  // Success
 * } catch (Exception e) {
 *     conn.rollback();  // Failure - revert all changes
 *     throw e;
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Loan
 * @see com.codeup.novabook.repo.ILoanRepository
 */
public interface ILoanService {

    /**
     * Creates a new loan with TRANSACTION MANAGEMENT.
     * <p>
     * This method implements the LibroNova requirement:
     * "Usar transacciones: Préstamo → setAutoCommit(false) → insertar préstamo → 
     * actualizar stock → commit() / rollback()"
     * </p>
     * 
     * <p><strong>Transaction Steps:</strong></p>
     * <ol>
     *   <li>Validate member exists and status = ACTIVE</li>
     *   <li>Check member hasn't reached maximum active loans</li>
     *   <li>Validate book exists, is_active = true, and stock > 0</li>
     *   <li>Decrease book stock by 1</li>
     *   <li>Create loan record with status = ACTIVE</li>
     *   <li>Commit transaction (or rollback on any error)</li>
     * </ol>
     * 
     * @param memberId the member ID requesting the loan
     * @param bookId the book ID to loan
     * @param dueDate the loan due date (must be in the future)
     * @return the created Loan with generated ID
     * @throws MemberNotFoundException if member doesn't exist or is not active
     * @throws BookNotFoundException if book doesn't exist
     * @throws BookNotAvailableException if book is inactive or stock = 0
     * @throws ValidationException if member has reached max loans or due date is invalid
     */
    Loan createLoan(Integer memberId, Integer bookId, LocalDate dueDate) 
            throws MemberNotFoundException, BookNotFoundException, 
                   BookNotAvailableException, ValidationException;

    /**
     * Returns a loan with TRANSACTION MANAGEMENT.
     * <p>
     * Transaction Steps:
     * </p>
     * <ol>
     *   <li>Validate loan exists and status = ACTIVE</li>
     *   <li>Calculate fine if overdue (days late * fine rate)</li>
     *   <li>Update loan: status = CLOSED, return_date = today, fine_amount</li>
     *   <li>Increase book stock by 1</li>
     *   <li>Commit transaction (or rollback on error)</li>
     * </ol>
     * 
     * @param loanId the loan ID to return
     * @param fineRatePerDay the fine rate per day overdue (e.g., 0.50 for $0.50/day)
     * @return the updated Loan with fine amount (if any)
     * @throws LoanNotFoundException if loan doesn't exist
     * @throws LoanAlreadyClosedException if loan is already closed
     */
    Loan returnLoan(Integer loanId, BigDecimal fineRatePerDay) 
            throws LoanNotFoundException, LoanAlreadyClosedException;

    /**
     * Extends a loan's due date.
     * Only ACTIVE loans can be extended.
     * New due date must be after current due date.
     * 
     * @param loanId the loan ID to extend
     * @param newDueDate the new due date (must be after current due date)
     * @return the updated Loan
     * @throws LoanNotFoundException if loan doesn't exist
     * @throws LoanAlreadyClosedException if loan is already closed
     * @throws ValidationException if new due date is invalid
     */
    Loan extendLoan(Integer loanId, LocalDate newDueDate) 
            throws LoanNotFoundException, LoanAlreadyClosedException, ValidationException;

    /**
     * Calculates the fine for an overdue loan.
     * <p>
     * Formula: fine = max(0, days_overdue) * fine_rate_per_day
     * </p>
     * <p>
     * Days overdue = current_date - due_date (if positive, else 0)
     * </p>
     * 
     * @param loanId the loan ID
     * @param fineRatePerDay the fine rate per day (e.g., 0.50)
     * @return the calculated fine amount (0.00 if not overdue)
     * @throws LoanNotFoundException if loan doesn't exist
     */
    BigDecimal calculateFine(Integer loanId, BigDecimal fineRatePerDay) 
            throws LoanNotFoundException;

    /**
     * Retrieves a loan by its unique ID.
     * 
     * @param loanId the loan ID
     * @return the Loan entity
     * @throws LoanNotFoundException if loan doesn't exist
     */
    Loan getLoanById(Integer loanId) throws LoanNotFoundException;

    /**
     * Retrieves all loans in the system.
     * 
     * @return list of all loans
     */
    List<Loan> getAllLoans();

    /**
     * Retrieves all active loans for a specific member.
     * 
     * @param memberId the member ID
     * @return list of active loans for the member
     * @throws MemberNotFoundException if member doesn't exist
     */
    List<Loan> getActiveLoansByMember(Integer memberId) throws MemberNotFoundException;

    /**
     * Retrieves all closed loans for a specific member (loan history).
     * 
     * @param memberId the member ID
     * @return list of closed loans for the member
     * @throws MemberNotFoundException if member doesn't exist
     */
    List<Loan> getClosedLoansByMember(Integer memberId) throws MemberNotFoundException;

    /**
     * Retrieves all loans for a specific book (usage history).
     * 
     * @param bookId the book ID
     * @return list of loans for the book
     * @throws BookNotFoundException if book doesn't exist
     */
    List<Loan> getLoansByBook(Integer bookId) throws BookNotFoundException;

    /**
     * Retrieves all currently overdue loans.
     * <p>
     * A loan is overdue if:
     * - status = ACTIVE
     * - due_date &lt; current_date
     * </p>
     * <p>
     * Used for CSV export as per LibroNova requirement:
     * "Exportar a CSV: Catálogo y préstamos vencidos"
     * </p>
     * 
     * @return list of overdue loans
     */
    List<Loan> getOverdueLoans();

    /**
     * Retrieves loans within a date range (for reporting).
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of loans created in the date range
     * @throws ValidationException if date range is invalid
     */
    List<Loan> getLoansByDateRange(LocalDate startDate, LocalDate endDate) 
            throws ValidationException;

    /**
     * Checks if a member is eligible for a new loan.
     * <p>
     * Eligibility criteria:
     * - Member status = ACTIVE
     * - Member has not reached maximum active loans (e.g., 3 loans)
     * - Member has no overdue loans
     * </p>
     * 
     * @param memberId the member ID
     * @param maxActiveLoans the maximum allowed active loans per member
     * @return true if eligible, false otherwise
     * @throws MemberNotFoundException if member doesn't exist
     */
    boolean isEligibleForLoan(Integer memberId, Integer maxActiveLoans) 
            throws MemberNotFoundException;

    /**
     * Counts active loans for a specific member.
     * 
     * @param memberId the member ID
     * @return count of active loans
     * @throws MemberNotFoundException if member doesn't exist
     */
    int countActiveLoansByMember(Integer memberId) throws MemberNotFoundException;

    /**
     * Checks if a member has any overdue loans.
     * 
     * @param memberId the member ID
     * @return true if member has overdue loans, false otherwise
     * @throws MemberNotFoundException if member doesn't exist
     */
    boolean hasOverdueLoans(Integer memberId) throws MemberNotFoundException;
}
