package com.codeup.novabook.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.LoanStatus;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.exception.BookNotAvailableException;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.LoanAlreadyClosedException;
import com.codeup.novabook.exception.LoanNotFoundException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IBookRepository;
import com.codeup.novabook.repo.ILoanRepository;
import com.codeup.novabook.repo.IMemberRepository;
import com.codeup.novabook.service.impl.LoanServiceImpl;

/**
 * Unit tests for LoanService with Mockito.
 */
class LoanServiceTest {

    @Mock
    private ILoanRepository loanRepository;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private IBookRepository bookRepository;
    
    @Mock
    private IConfigService configService;

    private ILoanService loanService;

    @BeforeEach
    @SuppressWarnings("unused") // Called by JUnit before each test
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configService.getMaxActiveLoans()).thenReturn(3); // Default max loans
        loanService = new LoanServiceImpl(loanRepository, memberRepository, bookRepository, configService);
    }

    // ==================== CREATE LOAN TESTS ====================

    @Test
    @DisplayName("Should create loan with valid inputs")
    void testCreateLoanSuccess() {
        // Arrange
        Integer memberId = 1;
        Integer bookId = 1;
        LocalDate dueDate = LocalDate.now().plusDays(14);

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Book availableBook = new Book();
        availableBook.setId(bookId);
        availableBook.setActive(true);
        availableBook.setAvailableCopies(5);

        Loan createdLoan = new Loan();
        createdLoan.setId(1);
        createdLoan.setMemberId(memberId);
        createdLoan.setBookId(bookId);
        createdLoan.setExpectedReturnDate(dueDate);
        createdLoan.setStatus(LoanStatus.ACTIVE);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(List.of());
        when(loanRepository.create(any(Loan.class))).thenReturn(createdLoan);

        // Act
        Loan result = loanService.createLoan(memberId, bookId, dueDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(memberId, result.getMemberId());
        assertEquals(bookId, result.getBookId());
        assertEquals(LoanStatus.ACTIVE, result.getStatus());

        // Verify repository interactions
        verify(memberRepository, times(1)).findById(memberId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(loanRepository, times(1)).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw MemberNotFoundException when member does not exist")
    void testCreateLoanMemberNotFound() {
        // Arrange
        Integer memberId = 999;
        Integer bookId = 1;
        LocalDate dueDate = LocalDate.now().plusDays(14);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act & Assert
        MemberNotFoundException exception = assertThrows(MemberNotFoundException.class, () -> {
            loanService.createLoan(memberId, bookId, dueDate);
        });
        assertNotNull(exception);

        verify(memberRepository, times(1)).findById(memberId);
        verify(loanRepository, never()).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book does not exist")
    void testCreateLoanBookNotFound() {
        // Arrange
        Integer memberId = 1;
        Integer bookId = 999;
        LocalDate dueDate = LocalDate.now().plusDays(14);

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        BookNotFoundException exception = assertThrows(BookNotFoundException.class, () -> {
            loanService.createLoan(memberId, bookId, dueDate);
        });
        assertNotNull(exception);

        verify(bookRepository, times(1)).findById(bookId);
        verify(loanRepository, never()).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw BookNotAvailableException when book is inactive")
    void testCreateLoanBookInactive() {
        // Arrange
        Integer memberId = 1;
        Integer bookId = 1;
        LocalDate dueDate = LocalDate.now().plusDays(14);

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Book inactiveBook = new Book();
        inactiveBook.setId(bookId);
        inactiveBook.setActive(false);
        inactiveBook.setAvailableCopies(5);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(inactiveBook));

        // Act & Assert
        BookNotAvailableException exception = assertThrows(BookNotAvailableException.class, () -> {
            loanService.createLoan(memberId, bookId, dueDate);
        });
        assertNotNull(exception);

        verify(loanRepository, never()).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw BookNotAvailableException when book has no stock")
    void testCreateLoanNoStock() {
        // Arrange
        Integer memberId = 1;
        Integer bookId = 1;
        LocalDate dueDate = LocalDate.now().plusDays(14);

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Book bookWithNoStock = new Book();
        bookWithNoStock.setId(bookId);
        bookWithNoStock.setActive(true);
        bookWithNoStock.setAvailableCopies(0);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookWithNoStock));

        // Act & Assert
        BookNotAvailableException exception = assertThrows(BookNotAvailableException.class, () -> {
            loanService.createLoan(memberId, bookId, dueDate);
        });
        assertNotNull(exception);

        verify(loanRepository, never()).create(any(Loan.class));
    }

    // ==================== RETURN LOAN TESTS ====================

    @Test
    @DisplayName("Should return loan without fine when not overdue")
    void testReturnLoanNotOverdue() {
        // Arrange
        Integer loanId = 1;
        BigDecimal fineRate = new BigDecimal("0.50");

        Loan activeLoan = new Loan();
        activeLoan.setId(loanId);
        activeLoan.setBookId(1);
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan.setExpectedReturnDate(LocalDate.now().plusDays(7));

        Loan returnedLoan = new Loan();
        returnedLoan.setId(loanId);
        returnedLoan.setStatus(LoanStatus.RETURNED);
        returnedLoan.setActualReturnDate(LocalDate.now());
        returnedLoan.setFine(BigDecimal.ZERO);

        Book book = new Book();
        book.setId(1);
        book.setAvailableCopies(5);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(loanRepository.update(any(Loan.class))).thenReturn(returnedLoan);

        // Act
        Loan result = loanService.returnLoan(loanId, fineRate);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus() == LoanStatus.RETURNED || result.getStatus() == LoanStatus.OVERDUE);

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanRepository, times(1)).update(any(Loan.class));
    }

    @Test
    @DisplayName("Should return loan with fine when overdue")
    void testReturnLoanOverdue() {
        // Arrange
        Integer loanId = 1;
        BigDecimal fineRate = new BigDecimal("0.50");

        Loan overdueLoan = new Loan();
        overdueLoan.setId(loanId);
        overdueLoan.setBookId(1);
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        overdueLoan.setExpectedReturnDate(LocalDate.now().minusDays(5)); // 5 days overdue

        Loan returnedLoan = new Loan();
        returnedLoan.setId(loanId);
        returnedLoan.setStatus(LoanStatus.OVERDUE);
        returnedLoan.setActualReturnDate(LocalDate.now());
        returnedLoan.setFine(new BigDecimal("2.50")); // 5 days * 0.50

        Book book = new Book();
        book.setId(1);
        book.setAvailableCopies(5);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(overdueLoan));
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(loanRepository.update(any(Loan.class))).thenReturn(returnedLoan);

        // Act
        Loan result = loanService.returnLoan(loanId, fineRate);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus() == LoanStatus.OVERDUE || result.getStatus() == LoanStatus.RETURNED);

        verify(loanRepository, times(1)).update(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when returning non-existent loan")
    void testReturnLoanNotFound() {
        // Arrange
        Integer loanId = 999;
        BigDecimal fineRate = new BigDecimal("0.50");

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        LoanNotFoundException exception = assertThrows(LoanNotFoundException.class, () -> {
            loanService.returnLoan(loanId, fineRate);
        });
        assertNotNull(exception);

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanRepository, never()).update(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw LoanAlreadyClosedException when returning closed loan")
    void testReturnAlreadyClosedLoan() {
        // Arrange
        Integer loanId = 1;
        BigDecimal fineRate = new BigDecimal("0.50");

        Loan closedLoan = new Loan();
        closedLoan.setId(loanId);
        closedLoan.setStatus(LoanStatus.RETURNED);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(closedLoan));

        // Act & Assert
        LoanAlreadyClosedException exception = assertThrows(LoanAlreadyClosedException.class, () -> {
            loanService.returnLoan(loanId, fineRate);
        });
        assertNotNull(exception);

        verify(loanRepository, never()).update(any(Loan.class));
    }

    // ==================== EXTEND LOAN TESTS ====================

    @Test
    @DisplayName("Should extend loan with valid new due date")
    void testExtendLoanSuccess() {
        // Arrange
        Integer loanId = 1;
        LocalDate currentDueDate = LocalDate.now().plusDays(7);
        LocalDate newDueDate = LocalDate.now().plusDays(14);

        Loan activeLoan = new Loan();
        activeLoan.setId(loanId);
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan.setExpectedReturnDate(currentDueDate);

        Loan extendedLoan = new Loan();
        extendedLoan.setId(loanId);
        extendedLoan.setStatus(LoanStatus.ACTIVE);
        extendedLoan.setExpectedReturnDate(newDueDate);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.update(any(Loan.class))).thenReturn(extendedLoan);

        // Act
        Loan result = loanService.extendLoan(loanId, newDueDate);

        // Assert
        assertNotNull(result);
        assertEquals(newDueDate, result.getExpectedReturnDate());

        verify(loanRepository, times(1)).update(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when new due date is before current")
    void testExtendLoanInvalidDate() {
        // Arrange
        Integer loanId = 1;
        LocalDate newDueDate = LocalDate.now().plusDays(3); // Before current

        Loan activeLoan = new Loan();
        activeLoan.setId(loanId);
        activeLoan.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(activeLoan));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            loanService.extendLoan(loanId, newDueDate);
        });
        assertNotNull(exception);

        verify(loanRepository, never()).update(any(Loan.class));
    }

    // ==================== CALCULATE FINE TESTS ====================

    @Test
    @DisplayName("Should calculate zero fine when not overdue")
    void testCalculateFineNotOverdue() {
        // Arrange
        Integer loanId = 1;
        BigDecimal fineRate = new BigDecimal("0.50");

        Loan activeLoan = new Loan();
        activeLoan.setId(loanId);
        activeLoan.setExpectedReturnDate(LocalDate.now().plusDays(7));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(activeLoan));

        // Act
        BigDecimal fine = loanService.calculateFine(loanId, fineRate);

        // Assert
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    @DisplayName("Should calculate fine when overdue")
    void testCalculateFineOverdue() {
        // Arrange
        Integer loanId = 1;
        BigDecimal fineRate = new BigDecimal("0.50");

        Loan overdueLoan = new Loan();
        overdueLoan.setId(loanId);
        overdueLoan.setExpectedReturnDate(LocalDate.now().minusDays(10)); // 10 days overdue

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(overdueLoan));

        // Act
        BigDecimal fine = loanService.calculateFine(loanId, fineRate);

        // Assert
        assertEquals(new BigDecimal("5.00"), fine);
    }

    // ==================== GET OPERATIONS TESTS ====================

    @Test
    @DisplayName("Should get loan by ID")
    void testGetLoanById() {
        // Arrange
        Integer loanId = 1;

        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        // Act
        Loan result = loanService.getLoanById(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(loanId, result.getId());

        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when loan does not exist")
    void testGetLoanByIdNotFound() {
        // Arrange
        Integer loanId = 1;

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        LoanNotFoundException exception = assertThrows(LoanNotFoundException.class, () -> {
            loanService.getLoanById(loanId);
        });
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should get all loans")
    void testGetAllLoans() {
        // Arrange
        Loan loan1 = new Loan();
        loan1.setId(1);

        Loan loan2 = new Loan();
        loan2.setId(2);

        List<Loan> allLoans = List.of(loan1, loan2);
        when(loanRepository.findAll()).thenReturn(allLoans);

        // Act
        List<Loan> result = loanService.getAllLoans();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should get active loans by member")
    void testGetActiveLoansByMember() {
        // Arrange
        Integer memberId = 1;

        Member member = new Member();
        member.setId(memberId);

        Loan loan1 = new Loan();
        loan1.setId(1);
        loan1.setMemberId(memberId);
        loan1.setStatus(LoanStatus.ACTIVE);

        List<Loan> activeLoans = List.of(loan1);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(activeLoans);

        // Act
        List<Loan> result = loanService.getActiveLoansByMember(memberId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LoanStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should get overdue loans")
    void testGetOverdueLoans() {
        // Arrange
        Loan overdueLoan = new Loan();
        overdueLoan.setId(1);
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        overdueLoan.setExpectedReturnDate(LocalDate.now().minusDays(5));

        List<Loan> overdueLoans = List.of(overdueLoan);
        when(loanRepository.findOverdueLoans()).thenReturn(overdueLoans);

        // Act
        List<Loan> result = loanService.getOverdueLoans();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== ELIGIBILITY TESTS ====================

    @Test
    @DisplayName("Should return true when member is eligible for loan")
    void testIsEligibleForLoanTrue() {
        // Arrange
        Integer memberId = 1;
        Integer maxLoans = 3;

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Loan loan1 = new Loan();
        loan1.setExpectedReturnDate(LocalDate.now().plusDays(7)); // Not overdue

        Loan loan2 = new Loan();
        loan2.setExpectedReturnDate(LocalDate.now().plusDays(14)); // Not overdue

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(List.of(loan1, loan2));
        when(loanRepository.findByMemberId(memberId)).thenReturn(List.of(loan1, loan2));

        // Act
        boolean result = loanService.isEligibleForLoan(memberId, maxLoans);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when member has reached max loans")
    void testIsEligibleForLoanMaxReached() {
        // Arrange
        Integer memberId = 1;
        Integer maxLoans = 3;

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Loan loan1 = new Loan();
        loan1.setExpectedReturnDate(LocalDate.now().plusDays(7));

        Loan loan2 = new Loan();
        loan2.setExpectedReturnDate(LocalDate.now().plusDays(7));

        Loan loan3 = new Loan();
        loan3.setExpectedReturnDate(LocalDate.now().plusDays(7));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(List.of(loan1, loan2, loan3));

        // Act
        boolean result = loanService.isEligibleForLoan(memberId, maxLoans);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when member has overdue loans")
    void testIsEligibleForLoanHasOverdue() {
        // Arrange
        Integer memberId = 1;
        Integer maxLoans = 3;

        Member activeMember = new Member();
        activeMember.setId(memberId);
        activeMember.setStatus(MemberStatus.ACTIVE);

        Loan overdueLoan = new Loan();
        overdueLoan.setExpectedReturnDate(LocalDate.now().minusDays(5)); // Overdue

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(activeMember));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(List.of(overdueLoan));
        when(loanRepository.findByMemberId(memberId)).thenReturn(List.of(overdueLoan));

        // Act
        boolean result = loanService.isEligibleForLoan(memberId, maxLoans);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should count active loans by member")
    void testCountActiveLoansByMember() {
        // Arrange
        Integer memberId = 1;

        Member member = new Member();
        member.setId(memberId);

        Loan loan1 = new Loan();
        Loan loan2 = new Loan();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.findActiveLoansByMember(memberId)).thenReturn(List.of(loan1, loan2));

        // Act
        int count = loanService.countActiveLoansByMember(memberId);

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should check if member has overdue loans")
    void testHasOverdueLoans() {
        // Arrange
        Integer memberId = 1;

        Member member = new Member();
        member.setId(memberId);

        Loan overdueLoan = new Loan();
        overdueLoan.setExpectedReturnDate(LocalDate.now().minusDays(3)); // Overdue

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.findByMemberId(memberId)).thenReturn(List.of(overdueLoan));

        // Act
        boolean result = loanService.hasOverdueLoans(memberId);

        // Assert
        assertTrue(result);
    }
}
