package com.codeup.novabook.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Book entity representing a book in the LibroNova catalog.
 * <p>
 * This class encapsulates all book-related information including bibliographic data,
 * inventory control (total and available copies), pricing, and active status.
 * Each book is uniquely identified by its ISBN code.
 * </p>
 * 
 * <p><b>Business Rules:</b></p>
 * <ul>
 *   <li>ISBN must be unique across the entire catalog</li>
 *   <li>Available copies cannot exceed total copies</li>
 *   <li>Available copies cannot be negative</li>
 *   <li>Only active books can be loaned</li>
 *   <li>Stock is automatically managed during loan/return operations</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * Book book = new Book();
 * book.setIsbn("978-3-16-148410-0");
 * book.setTitle("One Hundred Years of Solitude");
 * book.setAuthor("Gabriel García Márquez");
 * book.setCategory("Literature");
 * book.setTotalCopies(5);
 * book.setAvailableCopies(5);
 * book.setReferencePrice(new BigDecimal("45000"));
 * book.setActive(true);
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see Loan
 */
public class Book {
    
    private Integer id;
    private String isbn; // Unique identifier (e.g., "978-3-16-148410-0")
    private String title;
    private String author;
    private String category; // e.g., "Literature", "Science Fiction", "History"
    private Integer totalCopies; // Total number of book copies
    private Integer availableCopies; // Copies currently available for loan
    private BigDecimal referencePrice; // Reference price in local currency
    private Boolean active; // Whether the book is active in the catalog
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for Book.
     */
    public Book() {
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new Book with essential bibliographic information.
     * 
     * @param isbn the unique ISBN code
     * @param title the book title
     * @param author the book author
     * @param category the book category
     */
    public Book(String isbn, String title, String author, String category) {
        this();
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
    }

    /**
     * Checks if this book is available for loan.
     * A book is available if it's active and has at least one available copy.
     * 
     * @return true if the book can be loaned, false otherwise
     */
    public boolean isAvailableForLoan() {
        return this.active && this.availableCopies != null && this.availableCopies > 0;
    }

    /**
     * Checks if this book is active in the catalog.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return this.active != null && this.active;
    }

    /**
     * Decreases available copies by one (when loaned).
     * 
     * @throws IllegalStateException if no copies are available
     */
    public void decreaseAvailableCopies() {
        if (availableCopies == null || availableCopies <= 0) {
            throw new IllegalStateException("No available copies to loan");
        }
        this.availableCopies--;
    }

    /**
     * Increases available copies by one (when returned).
     * 
     * @throws IllegalStateException if would exceed total copies
     */
    public void increaseAvailableCopies() {
        if (availableCopies == null) {
            this.availableCopies = 0;
        }
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("Available copies cannot exceed total copies");
        }
        this.availableCopies++;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                ", referencePrice=" + referencePrice +
                ", active=" + active +
                '}';
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

