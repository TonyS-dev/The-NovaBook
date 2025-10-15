package com.codeup.novabook.service;

import java.util.List;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.ValidationException;

/**
 * Service interface for Book management operations.
 * <p>
 * This interface defines all business logic operations related to books in the library,
 * including catalog management, stock control, search capabilities, and availability checks.
 * </p>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li>Book registration and CRUD operations</li>
 *   <li>Stock management (validation and updates)</li>
 *   <li>ISBN uniqueness validation</li>
 *   <li>Search and filtering by title, author, category, ISBN</li>
 *   <li>Availability checks for loan operations</li>
 *   <li>Book status management (active/inactive)</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see com.codeup.novabook.repo.IBookRepository
 */
public interface IBookService {

    /**
     * Registers a new book in the catalog.
     * 
     * @param title the book title
     * @param author the book author  
     * @param isbn the International Standard Book Number
     * @param category the category/genre of the book
     * @param stock the initial stock quantity (must be >= 0)
     * @param referencePrice the reference price for the book (optional, can be null)
     * @return the registered Book with generated ID
     * @throws ValidationException if any validation fails
     */
    Book registerBook(String title, String author, String isbn, String category, Integer stock, 
                     java.math.BigDecimal referencePrice) 
            throws ValidationException;

    /**
     * Retrieves a book by its unique ID.
     * 
     * @param bookId the book ID
     * @return the Book entity
     * @throws BookNotFoundException if the book doesn't exist
     */
    Book getBookById(Integer bookId) throws BookNotFoundException;

    /**
     * Retrieves a book by its ISBN.
     * 
     * @param isbn the International Standard Book Number
     * @return the Book entity
     * @throws BookNotFoundException if the book doesn't exist
     */
    Book getBookByIsbn(String isbn) throws BookNotFoundException;

    /**
     * Retrieves all books in the catalog (including inactive).
     * 
     * @return list of all books
     */
    List<Book> getAllBooks();

    /**
     * Retrieves only active books in the catalog.
     * 
     * @return list of active books
     */
    List<Book> getActiveBooks();

    /**
     * Retrieves books by category.
     * 
     * @param category the category to filter by
     * @return list of books in the specified category
     */
    List<Book> getBooksByCategory(String category);

    /**
     * Searches books by title (case-insensitive, partial match).
     * 
     * @param titleFragment the title fragment to search for
     * @return list of books matching the title
     */
    List<Book> searchBooksByTitle(String titleFragment);

    /**
     * Searches books by author (case-insensitive, partial match).
     * 
     * @param authorFragment the author fragment to search for
     * @return list of books matching the author
     */
    List<Book> searchBooksByAuthor(String authorFragment);

    /**
     * Retrieves books with stock below a threshold (for reorder alerts).
     * 
     * @param threshold the stock threshold (e.g., 5 copies)
     * @return list of books with low stock
     */
    List<Book> getBooksWithLowStock(Integer threshold);

    /**
     * Updates an existing book in the catalog.
     * 
     * @param bookId the ID of the book to update
     * @param title the new title
     * @param author the new author
     * @param isbn the new ISBN (must be unique)
     * @param category the new category
     * @param stock the new total stock (must be >= loaned copies)
     * @param referencePrice the new reference price (optional, can be null)
     * @return the updated Book
     * @throws BookNotFoundException if the book doesn't exist
     * @throws ValidationException if validation fails
     */
    Book updateBook(Integer bookId, String title, String author, String isbn, 
                    String category, Integer stock, java.math.BigDecimal referencePrice) 
            throws BookNotFoundException, ValidationException;

    /**
     * Updates only the stock of a book (for stock adjustments without loan operations).
     * 
     * @param bookId the book ID
     * @param newStock the new stock quantity (must be >= 0)
     * @return the updated Book
     * @throws BookNotFoundException if the book doesn't exist
     * @throws ValidationException if stock is negative
     */
    Book updateStock(Integer bookId, Integer newStock) 
            throws BookNotFoundException, ValidationException;

    /**
     * Deactivates a book (soft delete - sets is_active = false).
     * Deactivated books cannot be loaned but remain in database.
     * 
     * @param bookId the book ID to deactivate
     * @throws BookNotFoundException if the book doesn't exist
     */
    void deactivateBook(Integer bookId) throws BookNotFoundException;

    /**
     * Reactivates a previously deactivated book.
     * 
     * @param bookId the book ID to reactivate
     * @throws BookNotFoundException if the book doesn't exist
     */
    void activateBook(Integer bookId) throws BookNotFoundException;

    /**
     * Checks if a book is available for loan.
     * A book is available if:
     * - It exists
     * - is_active = true
     * - stock > 0
     * 
     * @param bookId the book ID
     * @return true if available for loan, false otherwise
     * @throws BookNotFoundException if the book doesn't exist
     */
    boolean isAvailableForLoan(Integer bookId) throws BookNotFoundException;

    /**
     * Checks if an ISBN already exists in the catalog.
     * Used for validation before creating or updating books.
     * 
     * @param isbn the ISBN to check
     * @return true if ISBN exists, false otherwise
     */
    boolean isbnExists(String isbn);

    /**
     * Checks if an ISBN exists but belongs to a different book.
     * Used when updating a book's ISBN to ensure uniqueness.
     * 
     * @param isbn the ISBN to check
     * @param excludeBookId the book ID to exclude from the check
     * @return true if ISBN exists for another book, false otherwise
     */
    boolean isbnExistsForOtherBook(String isbn, Integer excludeBookId);
}
