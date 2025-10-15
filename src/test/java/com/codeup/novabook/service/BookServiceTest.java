package com.codeup.novabook.service;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IBookRepository;
import com.codeup.novabook.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService implementation.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>Book registration with ISBN validation</li>
 *   <li>Book updates with ISBN uniqueness checks</li>
 *   <li>Stock management validation</li>
 *   <li>Book deactivation/activation</li>
 *   <li>ISBN existence checks</li>
 *   <li>Availability checks for loans</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 */
@DisplayName("BookService Tests")
class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    private IBookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookService = new BookServiceImpl(bookRepository);
    }

    // ==================== BOOK REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should register book with valid inputs")
    void testRegisterBookWithValidInputs() throws Exception {
        // Arrange
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = 10;
        
        Book createdBook = new Book();
        createdBook.setId(1);
        createdBook.setTitle(title);
        createdBook.setAuthor(author);
        createdBook.setIsbn(isbn);
        createdBook.setCategory(category);
        createdBook.setTotalCopies(stock);
        createdBook.setAvailableCopies(stock);
        createdBook.setActive(true);
        
        when(bookRepository.existsByIsbn(isbn)).thenReturn(false);
        when(bookRepository.create(any(Book.class))).thenReturn(createdBook);

        // Act
        Book result = bookService.registerBook(title, author, isbn, category, stock, null);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        verify(bookRepository, times(1)).existsByIsbn(isbn);
        verify(bookRepository, times(1)).create(argThat(book ->
            book.getTitle().equals(title) &&
            book.getAuthor().equals(author) &&
            book.getIsbn().equals(isbn) &&
            book.getCategory().equals(category) &&
            book.getTotalCopies().equals(stock) &&
            book.getAvailableCopies().equals(stock) &&
            book.isActive()
        ));
    }

    @Test
    @DisplayName("Should throw ValidationException when title is empty")
    void testRegisterBookWithEmptyTitle() {
        // Arrange
        String title = "";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = 10;

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.registerBook(title, author, isbn, category, stock, null)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when author is empty")
    void testRegisterBookWithEmptyAuthor() {
        // Arrange
        String title = "Clean Code";
        String author = "";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = 10;

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.registerBook(title, author, isbn, category, stock, null)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when ISBN is invalid format")
    void testRegisterBookWithInvalidIsbn() {
        // Arrange
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "ABC-123"; // Invalid ISBN
        String category = "Programming";
        Integer stock = 10;

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.registerBook(title, author, isbn, category, stock, null)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when ISBN already exists")
    void testRegisterBookWithDuplicateIsbn() {
        // Arrange
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = 10;
        
        when(bookRepository.existsByIsbn(isbn)).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.registerBook(title, author, isbn, category, stock, null)
        );
        verify(bookRepository, times(1)).existsByIsbn(isbn);
        verify(bookRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when stock is negative")
    void testRegisterBookWithNegativeStock() {
        // Arrange
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = -5;

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.registerBook(title, author, isbn, category, stock, null)
        );
        verify(bookRepository, never()).create(any());
    }

    // ==================== BOOK UPDATE TESTS ====================

    @Test
    @DisplayName("Should update book with valid inputs")
    void testUpdateBookWithValidInputs() throws Exception {
        // Arrange
        Integer bookId = 1;
        String title = "Clean Code - Updated";
        String author = "Robert C. Martin";
        String isbn = "978-0132350884";
        String category = "Programming";
        Integer stock = 15;
        
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Clean Code");
        existingBook.setAuthor(author);
        existingBook.setIsbn(isbn);
        existingBook.setCategory(category);
        existingBook.setTotalCopies(10);
        existingBook.setAvailableCopies(10);
        existingBook.setActive(true);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));

        // Act
        bookService.updateBook(bookId, title, author, isbn, category, stock, null);

        // Assert
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(argThat(book ->
            book.getTitle().equals(title) &&
            book.getTotalCopies().equals(stock)
        ));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when updating non-existent book")
    void testUpdateNonExistentBook() {
        // Arrange
        Integer bookId = 999;
        String validIsbn = "978-0132350884";
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () ->
            bookService.updateBook(bookId, "Title", "Author", validIsbn, "Category", 10, null)
        );
        verify(bookRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when updating with ISBN from another book")
    void testUpdateBookWithDuplicateIsbn() {
        // Arrange
        Integer bookId = 1;
        String originalIsbn = "978-0132350884";
        String newIsbn = "978-0132350999";
        
        Book bookToUpdate = new Book();
        bookToUpdate.setId(bookId);
        bookToUpdate.setTitle("Original Title");
        bookToUpdate.setAuthor("Original Author");
        bookToUpdate.setIsbn(originalIsbn);
        bookToUpdate.setCategory("Category");
        bookToUpdate.setTotalCopies(10);
        bookToUpdate.setAvailableCopies(10);
        bookToUpdate.setActive(true);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookToUpdate));
        when(bookRepository.existsByIsbn(newIsbn)).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            bookService.updateBook(bookId, "Title", "Author", newIsbn, "Category", 10, null)
        );
        verify(bookRepository, never()).update(any());
    }

    // ==================== ISBN EXISTS TESTS ====================

    @Test
    @DisplayName("Should return true when ISBN exists")
    void testIsbnExistsReturnsTrueWhenExists() {
        // Arrange
        String isbn = "978-0132350884";
        when(bookRepository.existsByIsbn(isbn)).thenReturn(true);

        // Act
        boolean result = bookService.isbnExists(isbn);

        // Assert
        assertTrue(result);
        verify(bookRepository, times(1)).existsByIsbn(isbn);
    }

    @Test
    @DisplayName("Should return false when ISBN does not exist")
    void testIsbnExistsReturnsFalseWhenNotExists() {
        // Arrange
        String isbn = "978-0132350884";
        when(bookRepository.existsByIsbn(isbn)).thenReturn(false);

        // Act
        boolean result = bookService.isbnExists(isbn);

        // Assert
        assertFalse(result);
        verify(bookRepository, times(1)).existsByIsbn(isbn);
    }

    @Test
    @DisplayName("Should return true when ISBN exists for other book")
    void testIsbnExistsForOtherBookReturnsTrue() {
        // Arrange
        String isbn = "978-0132350884";
        Integer excludeBookId = 1;
        
        Book otherBook = new Book();
        otherBook.setId(2); // Different book
        otherBook.setIsbn(isbn);
        
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(otherBook));

        // Act
        boolean result = bookService.isbnExistsForOtherBook(isbn, excludeBookId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when ISBN exists for same book")
    void testIsbnExistsForOtherBookReturnsFalseForSameBook() {
        // Arrange
        String isbn = "978-0132350884";
        Integer bookId = 1;
        
        Book sameBook = new Book();
        sameBook.setId(bookId); // Same book
        sameBook.setIsbn(isbn);
        
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(sameBook));

        // Act
        boolean result = bookService.isbnExistsForOtherBook(isbn, bookId);

        // Assert
        assertFalse(result);
    }

    // ==================== DEACTIVATE/ACTIVATE TESTS ====================

    @Test
    @DisplayName("Should deactivate book successfully")
    void testDeactivateBook() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setActive(true);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.deactivateBook(bookId);

        // Assert
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(argThat(b -> !b.isActive()));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when deactivating non-existent book")
    void testDeactivateNonExistentBook() {
        // Arrange
        Integer bookId = 999;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () ->
            bookService.deactivateBook(bookId)
        );
        verify(bookRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should activate book successfully")
    void testActivateBook() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setActive(false);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.activateBook(bookId);

        // Assert
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).update(argThat(b -> b.isActive()));
    }

    // ==================== AVAILABILITY TESTS ====================

    @Test
    @DisplayName("Should return true when book is available for loan")
    void testIsAvailableForLoanReturnsTrue() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setActive(true);
        book.setAvailableCopies(5);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        boolean result = bookService.isAvailableForLoan(bookId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when book has no stock")
    void testIsAvailableForLoanReturnsFalseWhenNoStock() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setActive(true);
        book.setAvailableCopies(0);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        boolean result = bookService.isAvailableForLoan(bookId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when book is inactive")
    void testIsAvailableForLoanReturnsFalseWhenInactive() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setActive(false);
        book.setAvailableCopies(5);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        boolean result = bookService.isAvailableForLoan(bookId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when checking availability of non-existent book")
    void testIsAvailableForLoanThrowsExceptionForNonExistent() {
        // Arrange
        Integer bookId = 999;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () ->
            bookService.isAvailableForLoan(bookId)
        );
    }

    // ==================== GET BOOKS TESTS ====================

    @Test
    @DisplayName("Should return all active books")
    void testGetActiveBooks() {
        // Arrange
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("Book 1");
        book1.setActive(true);
        
        Book book2 = new Book();
        book2.setId(2);
        book2.setTitle("Book 2");
        book2.setActive(true);
        
        List<Book> activeBooks = List.of(book1, book2);
        when(bookRepository.findActiveBooks()).thenReturn(activeBooks);

        // Act
        List<Book> result = bookService.getActiveBooks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Book::isActive));
        verify(bookRepository, times(1)).findActiveBooks();
    }

    @Test
    @DisplayName("Should return book by ID")
    void testGetBookById() throws Exception {
        // Arrange
        Integer bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Clean Code");
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        Book result = bookService.getBookById(bookId);

        // Assert
        assertNotNull(result);
        assertEquals(bookId, result.getId());
        assertEquals("Clean Code", result.getTitle());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found by ID")
    void testGetBookByIdThrowsException() {
        // Arrange
        Integer bookId = 999;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () ->
            bookService.getBookById(bookId)
        );
    }
}
