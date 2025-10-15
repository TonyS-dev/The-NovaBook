package com.codeup.novabook.repo.impl;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.LoanStatus;
import com.codeup.novabook.jdbc.JdbcTemplateLight;
import com.codeup.novabook.jdbc.RowMapper;
import com.codeup.novabook.repo.ILoanRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of the ILoanRepository interface.
 * <p>
 * This repository manages {@link Loan} entities with comprehensive transaction
 * support for the library's lending operations. Handles loan lifecycle, overdue
 * tracking, and fine calculations. Simplified database operations with JdbcTemplateLight.
 * </p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Loan lifecycle management (create, active, return, overdue)</li>
 *   <li>Overdue loans tracking for CSV export</li>
 *   <li>Member loan history and restrictions</li>
 *   <li>Date range queries for reporting</li>
 *   <li>Active loan validation to prevent duplicate loans</li>
 * </ul>
 * 
 * <p><b>Transaction Support:</b></p>
 * <p>
 * This repository is designed to work within transactional contexts managed
 * by the service layer. Stock operations should be coordinated with
 * {@link com.codeup.novabook.repo.IBookRepository} within transactions.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see ILoanRepository
 * @see Loan
 */
public class LoanRepositoryImpl implements ILoanRepository {
    
    private static final Logger LOGGER = Logger.getLogger(LoanRepositoryImpl.class.getName());
    private final JdbcTemplateLight jdbcTemplate;
    
    /**
     * RowMapper for converting ResultSet to Loan entity.
     */
    private static final RowMapper<Loan> LOAN_MAPPER = rs -> {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setMemberId(rs.getInt("member_id"));
        loan.setBookId(rs.getInt("book_id"));
        
        Date loanDate = rs.getDate("loan_date");
        if (loanDate != null) {
            loan.setLoanDate(loanDate.toLocalDate());
        }
        
        Date expectedReturnDate = rs.getDate("expected_return_date");
        if (expectedReturnDate != null) {
            loan.setExpectedReturnDate(expectedReturnDate.toLocalDate());
        }
        
        Date actualReturnDate = rs.getDate("actual_return_date");
        if (actualReturnDate != null) {
            loan.setActualReturnDate(actualReturnDate.toLocalDate());
        }
        
        loan.setLoanDays(rs.getInt("loan_days"));
        loan.setFine(rs.getBigDecimal("fine"));
        loan.setStatus(LoanStatus.valueOf(rs.getString("status")));
        
        return loan;
    };
    
    public LoanRepositoryImpl(ConnectionFactory connectionFactory) {
        this.jdbcTemplate = new JdbcTemplateLight(connectionFactory);
    }
    
    @Override
    public Loan create(Loan loan) {
        String sql = "INSERT INTO loans (member_id, book_id, loan_date, expected_return_date, " +
                     "loan_days, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        try {
            return jdbcTemplate.txExecute(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, loan.getMemberId());
                    ps.setInt(2, loan.getBookId());
                    ps.setDate(3, Date.valueOf(loan.getLoanDate()));
                    ps.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
                    ps.setInt(5, loan.getLoanDays());
                    ps.setString(6, loan.getStatus().name());
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        loan.setId(rs.getInt("id"));
                    }
                    
                    LOGGER.log(Level.INFO, "Loan created: memberId={0}, bookId={1}", 
                              new Object[]{loan.getMemberId(), loan.getBookId()});
                    return loan;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating loan: " + loan, e);
            throw new RuntimeException("Failed to create loan", e);
        }
    }
    
    @Override
    public Optional<Loan> findById(Integer id) {
        String sql = "SELECT * FROM loans WHERE id = ?";
        
        try {
            List<Loan> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loan by ID: " + id, e);
            throw new RuntimeException("Failed to find loan", e);
        }
    }
    
    @Override
    public List<Loan> findAll() {
        String sql = "SELECT * FROM loans ORDER BY loan_date DESC";
        
        try {
            return jdbcTemplate.query(sql, null, LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all loans", e);
            throw new RuntimeException("Failed to fetch loans", e);
        }
    }
    
    @Override
    public Loan update(Loan loan) {
        String sql = "UPDATE loans SET member_id = ?, book_id = ?, loan_date = ?, " +
                     "expected_return_date = ?, actual_return_date = ?, loan_days = ?, " +
                     "fine = ?, status = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setInt(1, loan.getMemberId());
                    ps.setInt(2, loan.getBookId());
                    ps.setDate(3, Date.valueOf(loan.getLoanDate()));
                    ps.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
                    
                    if (loan.getActualReturnDate() != null) {
                        ps.setDate(5, Date.valueOf(loan.getActualReturnDate()));
                    } else {
                        ps.setNull(5, Types.DATE);
                    }
                    
                    ps.setInt(6, loan.getLoanDays());
                    ps.setBigDecimal(7, loan.getFine());
                    ps.setString(8, loan.getStatus().name());
                    ps.setInt(9, loan.getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Loan not found: " + loan.getId());
            }
            
            LOGGER.log(Level.INFO, "Loan updated: {0}", loan.getId());
            return loan;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating loan: " + loan.getId(), e);
            throw new RuntimeException("Failed to update loan", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        // IMPORTANT: Loans should NEVER be deleted!
        // Loans are historical records required for:
        // - Audit trails
        // - Member loan history
        // - Book circulation statistics
        // - Fine calculations and disputes
        // 
        // Instead, use loan status (ACTIVE, RETURNED, OVERDUE) to manage loan lifecycle.
        // This method throws an exception to prevent accidental data loss.
        
        LOGGER.log(Level.WARNING, "Attempted to delete loan: {0}. Operation rejected.", id);
        throw new UnsupportedOperationException(
            "Loans cannot be deleted. Loans are historical records. " +
            "Use loan status (RETURNED) to manage loan lifecycle."
        );
    }
    
    @Override
    public List<Loan> findByMemberId(Integer memberId) {
        String sql = "SELECT * FROM loans WHERE member_id = ? ORDER BY loan_date DESC";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, memberId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loans by member ID: " + memberId, e);
            throw new RuntimeException("Failed to find loans", e);
        }
    }
    
    @Override
    public List<Loan> findByBookId(Integer bookId) {
        String sql = "SELECT * FROM loans WHERE book_id = ? ORDER BY loan_date DESC";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, bookId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loans by book ID: " + bookId, e);
            throw new RuntimeException("Failed to find loans", e);
        }
    }
    
    @Override
    public List<Loan> findByStatus(LoanStatus status) {
        String sql = "SELECT * FROM loans WHERE status = ? ORDER BY loan_date DESC";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, status.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loans by status: " + status, e);
            throw new RuntimeException("Failed to find loans", e);
        }
    }
    
    @Override
    public List<Loan> findActiveLoans() {
        return findByStatus(LoanStatus.ACTIVE);
    }
    
    @Override
    public List<Loan> findActiveLoansByMember(Integer memberId) {
        String sql = "SELECT * FROM loans WHERE member_id = ? AND status = ? ORDER BY loan_date";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, memberId);
                        ps.setString(2, LoanStatus.ACTIVE.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding active loans by member: " + memberId, e);
            throw new RuntimeException("Failed to find active loans", e);
        }
    }
    
    @Override
    public List<Loan> findOverdueLoans() {
        String sql = "SELECT * FROM loans WHERE status = ? ORDER BY expected_return_date";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, LoanStatus.OVERDUE.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding overdue loans", e);
            throw new RuntimeException("Failed to find overdue loans", e);
        }
    }
    
    @Override
    public List<Loan> findLoansDueWithinDays(int days) {
        String sql = "SELECT * FROM loans WHERE status = ? " +
                     "AND expected_return_date BETWEEN CURRENT_DATE AND CURRENT_DATE + ? " +
                     "ORDER BY expected_return_date";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, LoanStatus.ACTIVE.name());
                        ps.setInt(2, days);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loans due within days: " + days, e);
            throw new RuntimeException("Failed to find loans", e);
        }
    }
    
    @Override
    public List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM loans WHERE loan_date BETWEEN ? AND ? ORDER BY loan_date DESC";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setDate(1, Date.valueOf(startDate));
                        ps.setDate(2, Date.valueOf(endDate));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                LOAN_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding loans by date range", e);
            throw new RuntimeException("Failed to find loans", e);
        }
    }
    
    @Override
    public boolean hasActiveLoansByMember(Integer memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, memberId);
                        ps.setString(2, LoanStatus.ACTIVE.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return !results.isEmpty() && results.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking active loans for member: " + memberId, e);
            throw new RuntimeException("Failed to check active loans", e);
        }
    }
    
    @Override
    public boolean hasActiveLoansByBook(Integer bookId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE book_id = ? AND status = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, bookId);
                        ps.setString(2, LoanStatus.ACTIVE.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return !results.isEmpty() && results.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking active loans for book: " + bookId, e);
            throw new RuntimeException("Failed to check active loans", e);
        }
    }
    
    @Override
    public int countLoansByMember(Integer memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, memberId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return results.isEmpty() ? 0 : results.get(0);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting loans for member: " + memberId, e);
            throw new RuntimeException("Failed to count loans", e);
        }
    }
    
    @Override
    public int countLoansByBook(Integer bookId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE book_id = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, bookId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return results.isEmpty() ? 0 : results.get(0);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting loans for book: " + bookId, e);
            throw new RuntimeException("Failed to count loans", e);
        }
    }
    
    @Override
    public JdbcTemplateLight getJdbcTemplate() {
        return this.jdbcTemplate;
    }
}
