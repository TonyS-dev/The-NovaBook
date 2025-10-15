package com.codeup.novabook.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Loan entity representing a book loan transaction in the LibroNova system.
 * <p>
 * This class encapsulates all information related to a book loan including
 * the member who borrowed it, the book, loan period, return dates, fines,
 * and current status. Loans can be ACTIVE, RETURNED, or OVERDUE.
 * </p>
 * 
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>Default loan period is 7 days</li>
 *   <li>Fine is calculated at 1500 per day overdue (configurable)</li>
 *   <li>Only ACTIVE members can request loans</li>
 *   <li>Only ACTIVE books with available copies can be loaned</li>
 *   <li>Stock is automatically updated on loan creation and return</li>
 *   <li>Status changes to OVERDUE if returned after expected date</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Loan loan = new Loan();
 * loan.setMemberId(1);
 * loan.setBookId(5);
 * loan.setLoanDate(LocalDate.now());
 * loan.setExpectedReturnDate(LocalDate.now().plusDays(7));
 * loan.setStatus(LoanStatus.ACTIVE);
 * 
 * // When returning the book
 * loan.setActualReturnDate(LocalDate.now());
 * loan.calculateFine(); // Automatic fine calculation
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Member
 * @see Book
 * @see LoanStatus
 */
public class Loan {
    
    private Integer id;
    private Integer memberId; // FK to members table
    private Integer bookId; // FK to books table
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate; // Null if not yet returned
    private Integer loanDays; // Number of days allowed for the loan (default: 7)
    private BigDecimal fine; // Fine amount if overdue
    private LoanStatus status;
    private String notes; // Additional observations
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for Loan.
     * Sets default values: loan days = 7, fine = 0, status = ACTIVE.
     */
    public Loan() {
        this.loanDate = LocalDate.now();
        this.loanDays = 7;
        this.expectedReturnDate = loanDate.plusDays(loanDays);
        this.fine = BigDecimal.ZERO;
        this.status = LoanStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new Loan with essential information.
     * 
     * @param memberId the ID of the member requesting the loan
     * @param bookId the ID of the book being loaned
     * @param loanDays the number of days for the loan period
     */
    public Loan(Integer memberId, Integer bookId, Integer loanDays) {
        this();
        this.memberId = memberId;
        this.bookId = bookId;
        this.loanDays = loanDays;
        this.expectedReturnDate = loanDate.plusDays(loanDays);
    }

    /**
     * Checks if the loan is currently active (not yet returned).
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return this.status == LoanStatus.ACTIVE;
    }

    /**
     * Checks if the loan has been returned.
     * 
     * @return true if returned, false otherwise
     */
    public boolean isReturned() {
        return this.status == LoanStatus.RETURNED || this.status == LoanStatus.OVERDUE;
    }

    /**
     * Checks if the loan is overdue (past expected return date and not returned).
     * 
     * @return true if overdue, false otherwise
     */
    public boolean isOverdue() {
        return this.status == LoanStatus.ACTIVE && 
               LocalDate.now().isAfter(this.expectedReturnDate);
    }

    /**
     * Calculates the number of days overdue.
     * Returns 0 if not overdue or already returned.
     * 
     * @return the number of days overdue
     */
    public long getDaysOverdue() {
        if (actualReturnDate != null) {
            // If returned, calculate based on actual return date
            return Math.max(0, ChronoUnit.DAYS.between(expectedReturnDate, actualReturnDate));
        } else {
            // If not returned, calculate based on today
            return Math.max(0, ChronoUnit.DAYS.between(expectedReturnDate, LocalDate.now()));
        }
    }

    /**
     * Calculates the fine based on overdue days.
     * Fine rate: 1500 per day (configurable via application.properties).
     * 
     * @param finePerDay the fine amount per overdue day
     * @return the calculated fine amount
     */
    public BigDecimal calculateFine(BigDecimal finePerDay) {
        long overdueDays = getDaysOverdue();
        if (overdueDays > 0) {
            this.fine = finePerDay.multiply(BigDecimal.valueOf(overdueDays));
            if (this.actualReturnDate != null) {
                this.status = LoanStatus.OVERDUE;
            }
        } else {
            this.fine = BigDecimal.ZERO;
            if (this.actualReturnDate != null && this.status != LoanStatus.OVERDUE) {
                this.status = LoanStatus.RETURNED;
            }
        }
        return this.fine;
    }

    /**
     * Marks the loan as returned with the current date.
     * Calculates fine if overdue.
     * 
     * @param finePerDay the fine amount per overdue day
     */
    public void markAsReturned(BigDecimal finePerDay) {
        this.actualReturnDate = LocalDate.now();
        calculateFine(finePerDay);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", bookId=" + bookId +
                ", loanDate=" + loanDate +
                ", expectedReturnDate=" + expectedReturnDate +
                ", actualReturnDate=" + actualReturnDate +
                ", loanDays=" + loanDays +
                ", fine=" + fine +
                ", status=" + status +
                ", daysOverdue=" + getDaysOverdue() +
                '}';
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public Integer getLoanDays() {
        return loanDays;
    }

    public void setLoanDays(Integer loanDays) {
        this.loanDays = loanDays;
    }

    public BigDecimal getFine() {
        return fine;
    }

    public void setFine(BigDecimal fine) {
        this.fine = fine;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

