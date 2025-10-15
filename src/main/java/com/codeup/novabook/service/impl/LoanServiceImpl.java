package com.codeup.novabook.service.impl;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.LoanStatus;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.exception.*;
import com.codeup.novabook.repo.IBookRepository;
import com.codeup.novabook.repo.ILoanRepository;
import com.codeup.novabook.repo.IMemberRepository;
import com.codeup.novabook.service.ILoanService;
import com.codeup.novabook.util.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for Loan management with full transaction support.
 * <p>
 * This is the CRITICAL service that implements atomic loan operations using
 * JdbcTemplateLight's transaction management (txExecute).
 * </p>
 */
public class LoanServiceImpl implements ILoanService {
    
    private static final Logger LOGGER = Logger.getLogger(LoanServiceImpl.class.getName());
    private final ILoanRepository loanRepository;
    private final IMemberRepository memberRepository;
    private final IBookRepository bookRepository;
    
    public LoanServiceImpl(ILoanRepository loanRepository, IMemberRepository memberRepository, 
                          IBookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }
    
    @Override
    public Loan createLoan(Integer memberId, Integer bookId, LocalDate dueDate) 
            throws MemberNotFoundException, BookNotFoundException, 
                   BookNotAvailableException, ValidationException {
        LOGGER.log(Level.INFO, "[POST /api/loans] Creating loan: memberId={0}, bookId={1}", 
                  new Object[]{memberId, bookId});
        
        try {
            // Validate inputs
            LocalDate loanDate = LocalDate.now();
            ValidationUtils.validateLoan(memberId, bookId, loanDate);
            
            if (dueDate == null || !dueDate.isAfter(loanDate)) {
                throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
            }
            
            // Execute loan creation in a TRANSACTION
            Loan[] createdLoan = new Loan[1]; // Array to capture result from lambda
            
            loanRepository.getJdbcTemplate().txExecute(conn -> {
                LOGGER.log(Level.INFO, "[TX] Starting loan creation transaction");
                
                // Step 1: Validate member exists and is ACTIVE
                Optional<Member> memberOpt = memberRepository.findById(memberId);
                if (memberOpt.isEmpty()) {
                    throw new MemberNotFoundException();
                }
                
                Member member = memberOpt.get();
                if (member.getStatus() != MemberStatus.ACTIVE) {
                    LOGGER.log(Level.WARNING, "[TX] Member is not active: {0}", memberId);
                    throw new ValidationException(ErrorCode.MEMBER_INACTIVE);
                }
                
                // Step 1b: Check member hasn't reached max active loans (3)
                int activeLoansCount = loanRepository.countLoansByMember(memberId);
                final int MAX_ACTIVE_LOANS = 3;
                if (activeLoansCount >= MAX_ACTIVE_LOANS) {
                    LOGGER.log(Level.WARNING, "[TX] Member {0} has reached max active loans: {1}/{2}",
                              new Object[]{memberId, activeLoansCount, MAX_ACTIVE_LOANS});
                    throw new ValidationException(ErrorCode.MAX_ACTIVE_LOANS_REACHED);
                }
                LOGGER.log(Level.INFO, "[TX] Member {0} has {1}/{2} active loans, eligible",
                          new Object[]{memberId, activeLoansCount, MAX_ACTIVE_LOANS});
                
                // Step 1c: Check member doesn't already have an active loan of this book
                List<Loan> memberActiveLoans = loanRepository.findActiveLoansByMember(memberId);
                boolean alreadyHasBook = memberActiveLoans.stream()
                        .anyMatch(loan -> loan.getBookId().equals(bookId));
                if (alreadyHasBook) {
                    LOGGER.log(Level.WARNING, "[TX] Member {0} already has an active loan of book {1}",
                              new Object[]{memberId, bookId});
                    throw new ValidationException(ErrorCode.DUPLICATE_BOOK_LOAN);
                }
                LOGGER.log(Level.INFO, "[TX] Member {0} doesn't have book {1} on loan, can proceed",
                          new Object[]{memberId, bookId});
                
                // Step 2: Check book exists, is active, and has stock
                Optional<Book> bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isEmpty()) {
                    throw new BookNotFoundException();
                }
                
                Book book = bookOpt.get();
                if (!book.isActive()) {
                    LOGGER.log(Level.WARNING, "[TX] Book is not active: {0}", bookId);
                    throw new BookNotAvailableException();
                }
                
                if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
                    LOGGER.log(Level.WARNING, "[TX] Book has no available copies: {0}", bookId);
                    throw new BookNotAvailableException();
                }
                
                // Step 3: Decrease book stock (atomic operation within transaction)
                boolean stockDecreased = bookRepository.decreaseAvailableCopies(bookId);
                if (!stockDecreased) {
                    LOGGER.log(Level.SEVERE, "[TX] Failed to decrease book stock");
                    throw new DatabaseException(ErrorCode.DATABASE_ERROR);
                }
                LOGGER.log(Level.INFO, "[TX] Decreased stock for book {0}", bookId);
                
                // Step 4: Create loan record
                Loan loan = new Loan();
                loan.setMemberId(memberId);
                loan.setBookId(bookId);
                loan.setLoanDate(loanDate);
                loan.setExpectedReturnDate(dueDate);
                loan.setStatus(LoanStatus.ACTIVE);
                loan.setCreatedAt(LocalDateTime.now());
                
                Loan created = loanRepository.create(loan);
                LOGGER.log(Level.INFO, "[TX] Created loan record with ID: {0}", created.getId());
                
                createdLoan[0] = created;
                
                LOGGER.log(Level.INFO, "[TX] Transaction committed successfully");
                return null; // Success - transaction will commit
            });
            
            LOGGER.log(Level.INFO, "[POST /api/loans] Loan created successfully with ID: {0}", 
                      createdLoan[0].getId());
            return createdLoan[0];
            
        } catch (MemberNotFoundException | BookNotFoundException | BookNotAvailableException | ValidationException e) {
            LOGGER.log(Level.WARNING, "[POST /api/loans] Validation error: {0}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/loans] Transaction failed", e);
            throw new DatabaseException(ErrorCode.TRANSACTION_FAILED);
        }
    }
    
    @Override
    public Loan returnLoan(Integer loanId, BigDecimal fineRatePerDay) 
            throws LoanNotFoundException, LoanAlreadyClosedException {
        LOGGER.log(Level.INFO, "[PATCH /api/loans/{0}/return] Returning loan", loanId);
        
        try {
            // Execute loan return in a TRANSACTION
            Loan[] returnedLoan = new Loan[1];
            
            loanRepository.getJdbcTemplate().txExecute(conn -> {
                LOGGER.log(Level.INFO, "[TX] Starting loan return transaction");
                
                // Step 1: Validate loan exists and is ACTIVE
                Optional<Loan> loanOpt = loanRepository.findById(loanId);
                if (loanOpt.isEmpty()) {
                    throw new LoanNotFoundException();
                }
                
                Loan loan = loanOpt.get();
                if (loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.OVERDUE) {
                    LOGGER.log(Level.WARNING, "[TX] Loan is already closed: {0}", loanId);
                    throw new LoanAlreadyClosedException();
                }
                
                // Step 2: Calculate fine if overdue
                LocalDate today = LocalDate.now();
                BigDecimal fine = BigDecimal.ZERO;
                LoanStatus newStatus = LoanStatus.RETURNED;
                
                if (today.isAfter(loan.getExpectedReturnDate())) {
                    long daysOverdue = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), today);
                    fine = fineRatePerDay.multiply(BigDecimal.valueOf(daysOverdue));
                    newStatus = LoanStatus.OVERDUE;
                    LOGGER.log(Level.INFO, "[TX] Loan overdue by {0} days, fine: {1}", 
                              new Object[]{daysOverdue, fine});
                }
                
                // Step 3: Update loan record
                loan.setActualReturnDate(today);
                loan.setStatus(newStatus);
                loan.setFine(fine);
                loan.setUpdatedAt(LocalDateTime.now());
                
                loanRepository.update(loan);
                LOGGER.log(Level.INFO, "[TX] Updated loan record to CLOSED");
                
                // Step 4: Increase book stock (atomic operation within transaction)
                boolean stockIncreased = bookRepository.increaseAvailableCopies(loan.getBookId());
                if (!stockIncreased) {
                    LOGGER.log(Level.SEVERE, "[TX] Failed to increase book stock");
                    throw new DatabaseException(ErrorCode.DATABASE_ERROR);
                }
                LOGGER.log(Level.INFO, "[TX] Increased stock for book {0}", loan.getBookId());
                
                returnedLoan[0] = loan;
                
                LOGGER.log(Level.INFO, "[TX] Transaction committed successfully");
                return null; // Success - transaction will commit
            });
            
            LOGGER.log(Level.INFO, "[PATCH /api/loans/{0}/return] Loan returned successfully", loanId);
            return returnedLoan[0];
            
        } catch (LoanNotFoundException | LoanAlreadyClosedException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/loans/return] Transaction failed", e);
            throw new DatabaseException(ErrorCode.TRANSACTION_FAILED);
        }
    }
    
    @Override
    public Loan extendLoan(Integer loanId, LocalDate newDueDate) 
            throws LoanNotFoundException, LoanAlreadyClosedException, ValidationException {
        LOGGER.log(Level.INFO, "[PATCH /api/loans/{0}/extend] Extending loan to {1}", 
                  new Object[]{loanId, newDueDate});
        
        try {
            Loan loan = getLoanById(loanId);
            
            if (loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.OVERDUE) {
                throw new LoanAlreadyClosedException();
            }
            
            if (newDueDate == null || !newDueDate.isAfter(loan.getExpectedReturnDate())) {
                throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
            }
            
            loan.setExpectedReturnDate(newDueDate);
            loan.setUpdatedAt(LocalDateTime.now());
            
            loanRepository.update(loan);
            LOGGER.log(Level.INFO, "[PATCH /api/loans/{0}/extend] Loan extended successfully", loanId);
            return loan;
            
        } catch (LoanNotFoundException | LoanAlreadyClosedException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/loans/extend] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public BigDecimal calculateFine(Integer loanId, BigDecimal fineRatePerDay) 
            throws LoanNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/loans/{0}/fine] Calculating fine", loanId);
        
        try {
            Loan loan = getLoanById(loanId);
            LocalDate today = LocalDate.now();
            
            if (!today.isAfter(loan.getExpectedReturnDate())) {
                return BigDecimal.ZERO; // Not overdue
            }
            
            long daysOverdue = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), today);
            BigDecimal fine = fineRatePerDay.multiply(BigDecimal.valueOf(daysOverdue));
            
            LOGGER.log(Level.INFO, "[GET /api/loans/{0}/fine] Fine calculated: {1}", 
                      new Object[]{loanId, fine});
            return fine;
            
        } catch (LoanNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/loans/fine] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Loan getLoanById(Integer loanId) throws LoanNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/loans/{0}] Fetching loan by ID", loanId);
        
        try {
            Optional<Loan> loan = loanRepository.findById(loanId);
            if (loan.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/loans/{0}] Loan not found", loanId);
                throw new LoanNotFoundException();
            }
            return loan.get();
        } catch (LoanNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/loans] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getAllLoans() {
        LOGGER.log(Level.INFO, "[GET /api/loans] Fetching all loans");
        
        try {
            List<Loan> loans = loanRepository.findAll();
            LOGGER.log(Level.INFO, "[GET /api/loans] Found {0} loans", loans.size());
            return loans;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/loans] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getActiveLoansByMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/active] Fetching active loans", memberId);
        
        try {
            // Validate member exists
            if (memberRepository.findById(memberId).isEmpty()) {
                throw new MemberNotFoundException();
            }
            
            List<Loan> loans = loanRepository.findActiveLoansByMember(memberId);
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/active] Found {1} active loans", 
                      new Object[]{memberId, loans.size()});
            return loans;
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/loans/active] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getClosedLoansByMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/history] Fetching closed loans", memberId);
        
        try {
            // Validate member exists
            if (memberRepository.findById(memberId).isEmpty()) {
                throw new MemberNotFoundException();
            }
            
            // Filter closed loans (RETURNED or OVERDUE status)
            List<Loan> allLoans = loanRepository.findByMemberId(memberId);
            List<Loan> closedLoans = allLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.OVERDUE)
                    .collect(java.util.stream.Collectors.toList());
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/history] Found {1} closed loans", 
                      new Object[]{memberId, closedLoans.size()});
            return closedLoans;
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/loans/history] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getLoansByBook(Integer bookId) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/books/{0}/loans] Fetching loans for book", bookId);
        
        try {
            // Validate book exists
            if (bookRepository.findById(bookId).isEmpty()) {
                throw new BookNotFoundException();
            }
            
            List<Loan> loans = loanRepository.findByBookId(bookId);
            LOGGER.log(Level.INFO, "[GET /api/books/{0}/loans] Found {1} loans", 
                      new Object[]{bookId, loans.size()});
            return loans;
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/loans] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getOverdueLoans() {
        LOGGER.log(Level.INFO, "[GET /api/loans/overdue] Fetching overdue loans");
        
        try {
            List<Loan> loans = loanRepository.findOverdueLoans();
            LOGGER.log(Level.INFO, "[GET /api/loans/overdue] Found {0} overdue loans", loans.size());
            return loans;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/loans/overdue] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Loan> getLoansByDateRange(LocalDate startDate, LocalDate endDate) 
            throws ValidationException {
        LOGGER.log(Level.INFO, "[GET /api/loans?start={0}&end={1}] Fetching loans by date range", 
                  new Object[]{startDate, endDate});
        
        try {
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
            }
            
            List<Loan> loans = loanRepository.findLoansByDateRange(startDate, endDate);
            LOGGER.log(Level.INFO, "[GET /api/loans] Found {0} loans in date range", loans.size());
            return loans;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/loans] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean isEligibleForLoan(Integer memberId, Integer maxActiveLoans) 
            throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Checking loan eligibility", memberId);
        
        try {
            // Check member exists and is ACTIVE
            Optional<Member> memberOpt = memberRepository.findById(memberId);
            if (memberOpt.isEmpty()) {
                throw new MemberNotFoundException();
            }
            
            Member member = memberOpt.get();
            if (member.getStatus() != MemberStatus.ACTIVE) {
                LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Member not active", memberId);
                return false;
            }
            
            // Check active loans count
            int activeLoans = loanRepository.countLoansByMember(memberId);
            if (activeLoans >= maxActiveLoans) {
                LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Max loans reached: {1}/{2}", 
                          new Object[]{memberId, activeLoans, maxActiveLoans});
                return false;
            }
            
            // Check for overdue loans
            List<Loan> allLoans = loanRepository.findByMemberId(memberId);
            List<Loan> overdueLoans = allLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE && 
                                   LocalDate.now().isAfter(loan.getExpectedReturnDate()))
                    .collect(java.util.stream.Collectors.toList());
            if (!overdueLoans.isEmpty()) {
                LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Has {1} overdue loans", 
                          new Object[]{memberId, overdueLoans.size()});
                return false;
            }
            
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Eligible for loan", memberId);
            return true;
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/eligible] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public int countActiveLoansByMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/count] Counting active loans", memberId);
        
        try {
            // Validate member exists
            if (memberRepository.findById(memberId).isEmpty()) {
                throw new MemberNotFoundException();
            }
            
            List<Loan> activeLoans = loanRepository.findActiveLoansByMember(memberId);
            int count = activeLoans.size();
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/count] Count: {1}", 
                      new Object[]{memberId, count});
            return count;
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/loans/count] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean hasOverdueLoans(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/overdue] Checking for overdue loans", memberId);
        
        try {
            // Validate member exists
            if (memberRepository.findById(memberId).isEmpty()) {
                throw new MemberNotFoundException();
            }
            
            List<Loan> allLoans = loanRepository.findByMemberId(memberId);
            boolean hasOverdue = allLoans.stream()
                    .anyMatch(loan -> loan.getStatus() == LoanStatus.ACTIVE && 
                                     LocalDate.now().isAfter(loan.getExpectedReturnDate()));
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/loans/overdue] Has overdue: {1}", 
                      new Object[]{memberId, hasOverdue});
            return hasOverdue;
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/loans/overdue] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
}
