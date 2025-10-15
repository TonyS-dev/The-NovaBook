package com.codeup.novabook.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.DatabaseException;
import com.codeup.novabook.exception.ErrorCode;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IBookRepository;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.util.ValidationUtils;

/**
 * Service implementation for Book management with ISBN validation and stock control.
 */
public class BookServiceImpl implements IBookService {
    
    private static final Logger LOGGER = Logger.getLogger(BookServiceImpl.class.getName());
    private final IBookRepository bookRepository;
    
    public BookServiceImpl(IBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public Book registerBook(String title, String author, String isbn, String category, Integer stock,
                            java.math.BigDecimal referencePrice) 
            throws ValidationException {
        LOGGER.log(Level.INFO, "[POST /api/books/register] Registering new book: {0}", title);
        
        try {
            // Use centralized validation
            ValidationUtils.validateBook(isbn, title, author, category);
            
            // Validate stock
            if (stock == null || stock < 0) {
                throw new ValidationException(ErrorCode.NEGATIVE_VALUE_NOT_ALLOWED);
            }
            
            // Check ISBN uniqueness
            if (bookRepository.existsByIsbn(isbn)) {
                LOGGER.log(Level.WARNING, "[POST /api/books/register] ISBN already exists: {0}", isbn);
                throw new ValidationException(ErrorCode.BOOK_ALREADY_EXISTS);
            }
            
            // Create book with decorator pattern (default values)
            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            // Convert String category to BookCategory enum (use fromDisplayName for CSV imports)
            book.setCategory(category != null ? com.codeup.novabook.domain.BookCategory.fromDisplayName(category) : null);
            book.setTotalCopies(stock);
            book.setAvailableCopies(stock);
            book.setReferencePrice(referencePrice); // Set price
            book.setActive(true);
            book.setCreatedAt(LocalDateTime.now());
            
            LOGGER.log(Level.INFO, "[POST /api/books/register] Applied default status: ACTIVE, copies: {0}, price: {1}", 
                      new Object[]{stock, referencePrice});
            
            Book created = bookRepository.create(book);
            LOGGER.log(Level.INFO, "[POST /api/books/register] Book registered successfully with ID: {0}", created.getId());
            return created;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/books/register] Registration error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Book getBookById(Integer bookId) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/books/{0}] Fetching book by ID", bookId);
        
        try {
            Optional<Book> book = bookRepository.findById(bookId);
            if (book.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/books/{0}] Book not found", bookId);
                throw new BookNotFoundException();
            }
            return book.get();
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Book getBookByIsbn(String isbn) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/books/isbn/{0}] Fetching book by ISBN", isbn);
        
        try {
            Optional<Book> book = bookRepository.findByIsbn(isbn);
            if (book.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/books/isbn/{0}] Book not found", isbn);
                throw new BookNotFoundException();
            }
            return book.get();
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/isbn] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> getAllBooks() {
        LOGGER.log(Level.INFO, "[GET /api/books] Fetching all books");
        
        try {
            List<Book> books = bookRepository.findAll();
            LOGGER.log(Level.INFO, "[GET /api/books] Found {0} books", books.size());
            return books;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> getActiveBooks() {
        LOGGER.log(Level.INFO, "[GET /api/books/active] Fetching active books");
        
        try {
            List<Book> books = bookRepository.findActiveBooks();
            LOGGER.log(Level.INFO, "[GET /api/books/active] Found {0} active books", books.size());
            return books;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/active] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> getBooksByCategory(String category) {
        LOGGER.log(Level.INFO, "[GET /api/books/category/{0}] Fetching books by category", category);
        
        try {
            List<Book> books = bookRepository.findByCategory(category);
            LOGGER.log(Level.INFO, "[GET /api/books/category/{0}] Found {1} books", 
                      new Object[]{category, books.size()});
            return books;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/category] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> searchBooksByTitle(String titleFragment) {
        LOGGER.log(Level.INFO, "[GET /api/books/search/title?q={0}] Searching books by title", titleFragment);
        
        try {
            List<Book> books = bookRepository.searchByTitle(titleFragment);
            LOGGER.log(Level.INFO, "[GET /api/books/search/title] Found {0} matching books", books.size());
            return books;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/search/title] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> searchBooksByAuthor(String authorFragment) {
        LOGGER.log(Level.INFO, "[GET /api/books/search/author?q={0}] Searching books by author", authorFragment);
        
        try {
            List<Book> books = bookRepository.findByAuthor(authorFragment);
            LOGGER.log(Level.INFO, "[GET /api/books/search/author] Found {0} matching books", books.size());
            return books;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/search/author] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Book> getBooksWithLowStock(Integer threshold) {
        LOGGER.log(Level.INFO, "[GET /api/books/low-stock?threshold={0}] Fetching books with low stock", threshold);
        
        try {
            // Filter books with available copies below threshold
            List<Book> allBooks = bookRepository.findAll();
            List<Book> lowStockBooks = allBooks.stream()
                    .filter(book -> book.getAvailableCopies() != null && book.getAvailableCopies() < threshold)
                    .collect(java.util.stream.Collectors.toList());
            LOGGER.log(Level.INFO, "[GET /api/books/low-stock] Found {0} books with low stock", lowStockBooks.size());
            return lowStockBooks;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/low-stock] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public Book updateBook(Integer bookId, String title, String author, String isbn, 
                          String category, Integer stock, java.math.BigDecimal referencePrice) 
            throws BookNotFoundException, ValidationException {
        LOGGER.log(Level.INFO, "[PUT /api/books/{0}] Updating book", bookId);
        
        try {
            // Validate input
            ValidationUtils.validateBook(isbn, title, author, category);
            
            if (stock == null || stock < 0) {
                throw new ValidationException(ErrorCode.NEGATIVE_VALUE_NOT_ALLOWED);
            }
            
            // Get existing book
            Book book = getBookById(bookId);
            
            // Check ISBN uniqueness if changed
            if (!book.getIsbn().equals(isbn) && bookRepository.existsByIsbn(isbn)) {
                LOGGER.log(Level.WARNING, "[PUT /api/books/{0}] ISBN already exists: {1}", 
                          new Object[]{bookId, isbn});
                throw new ValidationException(ErrorCode.BOOK_ALREADY_EXISTS);
            }
            
            // Calculate available copies adjustment
            Integer currentTotal = book.getTotalCopies();
            Integer currentAvailable = book.getAvailableCopies();
            Integer loaned = currentTotal - currentAvailable;
            Integer newAvailable = stock - loaned;
            
            if (newAvailable < 0) {
                LOGGER.log(Level.WARNING, "[PUT /api/books/{0}] Cannot reduce stock below loaned copies", bookId);
                throw new ValidationException(ErrorCode.BOOK_INSUFFICIENT_STOCK);
            }
            
            // Update book
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            // Convert String category to BookCategory enum
            book.setCategory(category != null ? com.codeup.novabook.domain.BookCategory.fromDatabaseValue(category) : null);
            book.setTotalCopies(stock);
            book.setAvailableCopies(newAvailable);
            book.setReferencePrice(referencePrice); // Update price
            book.setUpdatedAt(LocalDateTime.now());
            
            bookRepository.update(book);
            LOGGER.log(Level.INFO, "[PUT /api/books/{0}] Book updated successfully (price: {1})", 
                      new Object[]{bookId, referencePrice});
            return book;
            
        } catch (BookNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PUT /api/books] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public Book updateStock(Integer bookId, Integer newStock) 
            throws BookNotFoundException, ValidationException {
        LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/stock] Updating stock to {1}", 
                  new Object[]{bookId, newStock});
        
        try {
            if (newStock == null || newStock < 0) {
                throw new ValidationException(ErrorCode.NEGATIVE_VALUE_NOT_ALLOWED);
            }
            
            Book book = getBookById(bookId);
            
            // Calculate loaned copies
            Integer currentTotal = book.getTotalCopies();
            Integer currentAvailable = book.getAvailableCopies();
            Integer loaned = currentTotal - currentAvailable;
            Integer newAvailable = newStock - loaned;
            
            if (newAvailable < 0) {
                LOGGER.log(Level.WARNING, "[PATCH /api/books/{0}/stock] Cannot reduce stock below loaned copies", bookId);
                throw new ValidationException(ErrorCode.BOOK_INSUFFICIENT_STOCK);
            }
            
            book.setTotalCopies(newStock);
            book.setAvailableCopies(newAvailable);
            book.setUpdatedAt(LocalDateTime.now());
            
            bookRepository.update(book);
            LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/stock] Stock updated successfully", bookId);
            return book;
            
        } catch (BookNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/books/stock] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public void deactivateBook(Integer bookId) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/deactivate] Deactivating book", bookId);
        
        try {
            Book book = getBookById(bookId);
            book.setActive(false);
            book.setUpdatedAt(LocalDateTime.now());
            
            bookRepository.update(book);
            LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/deactivate] Book deactivated successfully", bookId);
            
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/books/deactivate] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public void activateBook(Integer bookId) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/activate] Activating book", bookId);
        
        try {
            Book book = getBookById(bookId);
            book.setActive(true);
            book.setUpdatedAt(LocalDateTime.now());
            
            bookRepository.update(book);
            LOGGER.log(Level.INFO, "[PATCH /api/books/{0}/activate] Book activated successfully", bookId);
            
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/books/activate] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean isAvailableForLoan(Integer bookId) throws BookNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/books/{0}/available] Checking availability", bookId);
        
        try {
            Book book = getBookById(bookId);
            boolean available = book.isAvailableForLoan();
            LOGGER.log(Level.INFO, "[GET /api/books/{0}/available] Availability: {1}", 
                      new Object[]{bookId, available});
            return available;
            
        } catch (BookNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/available] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean isbnExists(String isbn) {
        LOGGER.log(Level.INFO, "[GET /api/books/exists/isbn/{0}] Checking ISBN existence", isbn);
        
        try {
            return bookRepository.existsByIsbn(isbn);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/exists/isbn] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean isbnExistsForOtherBook(String isbn, Integer excludeBookId) {
        LOGGER.log(Level.INFO, "[GET /api/books/exists/isbn/{0}?exclude={1}] Checking ISBN uniqueness", 
                  new Object[]{isbn, excludeBookId});
        
        try {
            Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
            if (existingBook.isEmpty()) {
                return false;
            }
            return !existingBook.get().getId().equals(excludeBookId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/books/exists/isbn] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
}
