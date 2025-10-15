package com.codeup.novabook.repo.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.BookCategory;
import com.codeup.novabook.jdbc.JdbcTemplateLight;
import com.codeup.novabook.jdbc.RowMapper;
import com.codeup.novabook.repo.IBookRepository;

/**
 * JDBC implementation of the IBookRepository interface using JdbcTemplateLight.
 * <p>
 * This repository manages {@link Book} entities with inventory control,
 * availability tracking, and catalog search capabilities. Critical for
 * loan transaction support with atomic stock operations.
 * </p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>ISBN uniqueness validation</li>
 *   <li>Atomic stock increase/decrease operations</li>
 *   <li>Multi-criteria search (title, author, category, ISBN)</li>
 *   <li>Availability filtering for loan eligibility</li>
 *   <li>Active/inactive catalog management</li>
 *   <li>Simplified database operations with JdbcTemplateLight</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see IBookRepository
 * @see Book
 */
public class BookRepositoryImpl implements IBookRepository {
    
    private static final Logger LOGGER = Logger.getLogger(BookRepositoryImpl.class.getName());
    private final JdbcTemplateLight jdbcTemplate;
    
    /**
     * RowMapper for converting ResultSet to Book entity.
     */
    private static final RowMapper<Book> BOOK_MAPPER = rs -> {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        
        // Convert database ENUM value to BookCategory
        String categoryValue = rs.getString("category");
        if (categoryValue != null) {
            book.setCategory(BookCategory.fromDatabaseValue(categoryValue));
        }
        
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setReferencePrice(rs.getBigDecimal("reference_price"));
        book.setActive(rs.getBoolean("is_active"));
        return book;
    };
    
    public BookRepositoryImpl(ConnectionFactory connectionFactory) {
        this.jdbcTemplate = new JdbcTemplateLight(connectionFactory);
    }
    
    @Override
    public Book create(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category, total_copies, " +
                     "available_copies, reference_price, is_active) " +
                     "VALUES (?, ?, ?, ?::book_category, ?, ?, ?, ?) RETURNING id";
        
        try {
            return jdbcTemplate.txExecute(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setString(4, book.getCategory() != null ? book.getCategory().toDatabaseValue() : null);
                    ps.setInt(5, book.getTotalCopies());
                    ps.setInt(6, book.getAvailableCopies());
                    ps.setBigDecimal(7, book.getReferencePrice());
                    ps.setBoolean(8, book.getActive());
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        book.setId(rs.getInt("id"));
                    }
                    
                    LOGGER.log(Level.INFO, "Book created: {0}", book.getIsbn());
                    return book;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating book: " + book.getIsbn(), e);
            throw new RuntimeException("Failed to create book", e);
        }
    }
    
    @Override
    public Optional<Book> findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        
        try {
            List<Book> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                BOOK_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding book by ID: " + id, e);
            throw new RuntimeException("Failed to find book", e);
        }
    }
    
    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, null, BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all books", e);
            throw new RuntimeException("Failed to fetch books", e);
        }
    }
    
    @Override
    public Book update(Book book) {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, category = ?::book_category, " +
                     "total_copies = ?, available_copies = ?, reference_price = ?, is_active = ? " +
                     "WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setString(4, book.getCategory() != null ? book.getCategory().toDatabaseValue() : null);
                    ps.setInt(5, book.getTotalCopies());
                    ps.setInt(6, book.getAvailableCopies());
                    ps.setBigDecimal(7, book.getReferencePrice());
                    ps.setBoolean(8, book.getActive());
                    ps.setInt(9, book.getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Book not found: " + book.getId());
            }
            
            LOGGER.log(Level.INFO, "Book updated: {0}", book.getIsbn());
            return book;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book: " + book.getId(), e);
            throw new RuntimeException("Failed to update book", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        // Soft delete: set is_active to FALSE instead of removing the record
        // Books should be deactivated, not deleted, to preserve loan history
        String sql = "UPDATE books SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setInt(1, id);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Book not found: " + id);
            }
            
            LOGGER.log(Level.INFO, "Book soft deleted (deactivated): {0}", id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting book: " + id, e);
            throw new RuntimeException("Failed to delete book", e);
        }
    }
    
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        
        try {
            List<Book> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, isbn);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                BOOK_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding book by ISBN: " + isbn, e);
            throw new RuntimeException("Failed to find book", e);
        }
    }
    
    @Override
    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, isbn);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return !results.isEmpty() && results.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking ISBN: " + isbn, e);
            throw new RuntimeException("Failed to check ISBN", e);
        }
    }
    
    @Override
    public List<Book> findByCategory(String category) {
        String sql = "SELECT * FROM books WHERE LOWER(category) = LOWER(?) ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, category);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding books by category: " + category, e);
            throw new RuntimeException("Failed to find books", e);
        }
    }
    
    @Override
    public List<Book> findByAuthor(String author) {
        String sql = "SELECT * FROM books WHERE LOWER(author) LIKE LOWER(?) ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, "%" + author + "%");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding books by author: " + author, e);
            throw new RuntimeException("Failed to find books", e);
        }
    }
    
    @Override
    public List<Book> findActiveBooks() {
        String sql = "SELECT * FROM books WHERE is_active = true ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, null, BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active books", e);
            throw new RuntimeException("Failed to fetch active books", e);
        }
    }
    
    @Override
    public List<Book> findAvailableBooks() {
        String sql = "SELECT * FROM books WHERE is_active = true AND available_copies > 0 ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, null, BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding available books", e);
            throw new RuntimeException("Failed to fetch available books", e);
        }
    }
    
    @Override
    public List<String> findAllCategories() {
        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL ORDER BY category";
        
        try {
            return jdbcTemplate.query(sql, null, rs -> rs.getString("category"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all categories", e);
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }
    
    @Override
    public List<Book> searchByTitle(String searchTerm) {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) ORDER BY title";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, "%" + searchTerm + "%");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                BOOK_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books by title: " + searchTerm, e);
            throw new RuntimeException("Failed to search books", e);
        }
    }
    
    @Override
    public boolean updateActiveStatus(Integer bookId, boolean active) {
        String sql = "UPDATE books SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setBoolean(1, active);
                    ps.setInt(2, bookId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Book active status updated: bookId={0}, active={1}", 
                          new Object[]{bookId, active});
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book active status: " + bookId, e);
            throw new RuntimeException("Failed to update active status", e);
        }
    }
    
    @Override
    public boolean decreaseAvailableCopies(Integer bookId) {
        String sql = "UPDATE books SET available_copies = available_copies - 1, " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND available_copies > 0";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setInt(1, bookId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Book available copies decreased: bookId={0}", bookId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error decreasing available copies: " + bookId, e);
            throw new RuntimeException("Failed to decrease available copies", e);
        }
    }
    
    @Override
    public boolean increaseAvailableCopies(Integer bookId) {
        String sql = "UPDATE books SET available_copies = available_copies + 1, " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND available_copies < total_copies";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setInt(1, bookId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Book available copies increased: bookId={0}", bookId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error increasing available copies: " + bookId, e);
            throw new RuntimeException("Failed to increase available copies", e);
        }
    }
}
