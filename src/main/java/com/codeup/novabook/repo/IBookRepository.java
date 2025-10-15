package com.codeup.novabook.repo;

import com.codeup.novabook.domain.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entity operations.
 * <p>
 * Extends the generic repository with Book-specific query methods including
 * ISBN lookups, category/author filtering, availability checks, and stock management.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Book CRUD operations</li>
 *   <li>ISBN uniqueness validation</li>
 *   <li>Catalog filtering (category, author)</li>
 *   <li>Stock management (increase/decrease available copies)</li>
 *   <li>Availability queries for loan operations</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IBookRepository bookRepo = new BookRepositoryImpl();
 * 
 * // Find book by ISBN
 * Optional<Book> book = bookRepo.findByIsbn("978-3-16-148410-0");
 * 
 * // Filter by category
 * List<Book> sciFiBooks = bookRepo.findByCategory("Science Fiction");
 * 
 * // Get available books
 * List<Book> availableBooks = bookRepo.findAvailableBooks();
 * 
 * // Update stock (during loan)
 * bookRepo.decreaseAvailableCopies(bookId);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Book
 * @see IGeneralRepository
 */
public interface IBookRepository extends IGeneralRepository<Book, Integer> {
    
    /**
     * Finds a book by its ISBN code.
     * <p>
     * ISBN is unique in the system.
     * </p>
     * 
     * @param isbn the ISBN code to search for
     * @return an Optional containing the book if found, empty otherwise
     */
    Optional<Book> findByIsbn(String isbn);
    
    /**
     * Checks if a book with the given ISBN already exists.
     * <p>
     * Used for validation before creating new books.
     * </p>
     * 
     * @param isbn the ISBN to check
     * @return true if a book with this ISBN exists, false otherwise
     */
    boolean existsByIsbn(String isbn);
    
    /**
     * Finds all books in a specific category.
     * 
     * @param category the category to filter by
     * @return list of books in the specified category
     */
    List<Book> findByCategory(String category);
    
    /**
     * Finds all books by a specific author.
     * <p>
     * Case-insensitive partial match search.
     * </p>
     * 
     * @param author the author name to search for
     * @return list of books by the specified author
     */
    List<Book> findByAuthor(String author);
    
    /**
     * Finds all active books in the catalog.
     * <p>
     * Active books are available for display and loan operations.
     * </p>
     * 
     * @return list of active books
     */
    List<Book> findActiveBooks();
    
    /**
     * Finds all books that have available copies for loan.
     * <p>
     * Returns only active books with availableCopies > 0.
     * </p>
     * 
     * @return list of available books
     */
    List<Book> findAvailableBooks();
    
    /**
     * Gets all distinct categories in the catalog.
     * <p>
     * Useful for filtering and category management.
     * </p>
     * 
     * @return list of unique category names
     */
    List<String> findAllCategories();
    
    /**
     * Searches books by title.
     * <p>
     * Case-insensitive partial match search.
     * </p>
     * 
     * @param title the title to search for
     * @return list of books matching the search criteria
     */
    List<Book> searchByTitle(String title);
    
    /**
     * Updates the active status of a book.
     * <p>
     * Used for activating/deactivating books in the catalog.
     * </p>
     * 
     * @param bookId the ID of the book to update
     * @param active the new active status
     * @return true if updated successfully, false otherwise
     */
    boolean updateActiveStatus(Integer bookId, boolean active);
    
    /**
     * Decreases the available copies by 1 (when a book is loaned).
     * <p>
     * Used during loan creation. Should be called within a transaction.
     * </p>
     * 
     * @param bookId the ID of the book
     * @return true if updated successfully, false otherwise
     * @throws IllegalStateException if no copies are available
     */
    boolean decreaseAvailableCopies(Integer bookId);
    
    /**
     * Increases the available copies by 1 (when a book is returned).
     * <p>
     * Used during loan return. Should be called within a transaction.
     * </p>
     * 
     * @param bookId the ID of the book
     * @return true if updated successfully, false otherwise
     * @throws IllegalStateException if would exceed total copies
     */
    boolean increaseAvailableCopies(Integer bookId);
}
