package com.codeup.novabook.repo.impl;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.jdbc.JdbcTemplateLight;
import com.codeup.novabook.jdbc.RowMapper;
import com.codeup.novabook.repo.IMemberRepository;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of the IMemberRepository interface using JdbcTemplateLight.
 * <p>
 * This repository manages {@link Member} entities with support for unique
 * document ID validation, status management, and member search capabilities.
 * </p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Document ID and email uniqueness validation</li>
 *   <li>Member status management (ACTIVE, INACTIVE, SUSPENDED)</li>
 *   <li>Full-text search by name</li>
 *   <li>Active members filtering for loan eligibility</li>
 *   <li>Simplified database operations with JdbcTemplateLight</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see IMemberRepository
 * @see Member
 */
public class MemberRepositoryImpl implements IMemberRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MemberRepositoryImpl.class.getName());
    private final JdbcTemplateLight jdbcTemplate;
    
    /**
     * RowMapper for converting ResultSet to Member entity.
     */
    private static final RowMapper<Member> MEMBER_MAPPER = rs -> {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setDocumentId(rs.getString("document_id"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setAddress(rs.getString("address"));
        member.setStatus(MemberStatus.valueOf(rs.getString("status")));
        
        Date registrationDate = rs.getDate("registration_date");
        if (registrationDate != null) {
            member.setRegistrationDate(registrationDate.toLocalDate());
        }
        
        return member;
    };
    
    public MemberRepositoryImpl(ConnectionFactory connectionFactory) {
        this.jdbcTemplate = new JdbcTemplateLight(connectionFactory);
    }
    
    @Override
    public Member create(Member member) {
        String sql = "INSERT INTO members (first_name, last_name, document_id, email, phone, address, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, registration_date";
        
        try {
            return jdbcTemplate.txExecute(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, member.getFirstName());
                    ps.setString(2, member.getLastName());
                    ps.setString(3, member.getDocumentId());
                    ps.setString(4, member.getEmail());
                    ps.setString(5, member.getPhone());
                    ps.setString(6, member.getAddress());
                    ps.setString(7, member.getStatus().name());
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        member.setId(rs.getInt("id"));
                        member.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
                    }
                    
                    LOGGER.log(Level.INFO, "Member created: {0}", member.getDocumentId());
                    return member;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating member: " + member.getDocumentId(), e);
            throw new RuntimeException("Failed to create member", e);
        }
    }
    
    @Override
    public Optional<Member> findById(Integer id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        
        try {
            List<Member> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                MEMBER_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding member by ID: " + id, e);
            throw new RuntimeException("Failed to find member", e);
        }
    }
    
    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM members ORDER BY registration_date DESC";
        
        try {
            return jdbcTemplate.query(sql, null, MEMBER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all members", e);
            throw new RuntimeException("Failed to fetch members", e);
        }
    }
    
    @Override
    public Member update(Member member) {
        String sql = "UPDATE members SET first_name = ?, last_name = ?, document_id = ?, " +
                     "email = ?, phone = ?, address = ?, status = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, member.getFirstName());
                    ps.setString(2, member.getLastName());
                    ps.setString(3, member.getDocumentId());
                    ps.setString(4, member.getEmail());
                    ps.setString(5, member.getPhone());
                    ps.setString(6, member.getAddress());
                    ps.setString(7, member.getStatus().name());
                    ps.setInt(8, member.getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Member not found: " + member.getId());
            }
            
            LOGGER.log(Level.INFO, "Member updated: {0}", member.getDocumentId());
            return member;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member: " + member.getId(), e);
            throw new RuntimeException("Failed to update member", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        // Soft delete: update status to DELETED instead of removing the record
        String sql = "UPDATE members SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, MemberStatus.DELETED.name());
                    ps.setInt(2, id);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Member not found: " + id);
            }
            
            LOGGER.log(Level.INFO, "Member soft deleted: {0}", id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting member: " + id, e);
            throw new RuntimeException("Failed to delete member", e);
        }
    }
    
    @Override
    public Optional<Member> findByDocumentId(String documentId) {
        String sql = "SELECT * FROM members WHERE document_id = ?";
        
        try {
            List<Member> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, documentId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                MEMBER_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding member by document ID: " + documentId, e);
            throw new RuntimeException("Failed to find member", e);
        }
    }
    
    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM members WHERE email = ?";
        
        try {
            List<Member> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, email);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                MEMBER_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding member by email: " + email, e);
            throw new RuntimeException("Failed to find member", e);
        }
    }
    
    @Override
    public boolean existsByDocumentId(String documentId) {
        String sql = "SELECT COUNT(*) FROM members WHERE document_id = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, documentId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return !results.isEmpty() && results.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking document ID: " + documentId, e);
            throw new RuntimeException("Failed to check document ID", e);
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM members WHERE email = ?";
        
        try {
            List<Integer> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, email);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> rs.getInt(1));
            return !results.isEmpty() && results.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking email: " + email, e);
            throw new RuntimeException("Failed to check email", e);
        }
    }
    
    @Override
    public List<Member> findByStatus(MemberStatus status) {
        String sql = "SELECT * FROM members WHERE status = ? ORDER BY registration_date DESC";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, status.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                MEMBER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding members by status: " + status, e);
            throw new RuntimeException("Failed to find members", e);
        }
    }
    
    @Override
    public List<Member> findActiveMembers() {
        return findByStatus(MemberStatus.ACTIVE);
    }
    
    @Override
    public boolean updateStatus(Integer memberId, MemberStatus status) {
        String sql = "UPDATE members SET status = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, status.name());
                    ps.setInt(2, memberId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Member status updated: memberId={0}, status={1}", 
                          new Object[]{memberId, status});
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member status: " + memberId, e);
            throw new RuntimeException("Failed to update status", e);
        }
    }
    
    @Override
    public boolean updateEmail(Integer memberId, String newEmail) {
        String sql = "UPDATE members SET email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newEmail);
                    ps.setInt(2, memberId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Member email updated: memberId={0}, newEmail={1}", 
                          new Object[]{memberId, newEmail});
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member email: " + memberId, e);
            throw new RuntimeException("Failed to update email", e);
        }
    }
    
    @Override
    public boolean updatePhone(Integer memberId, String newPhone) {
        String sql = "UPDATE members SET phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newPhone);
                    ps.setInt(2, memberId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Member phone updated: memberId={0}", memberId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member phone: " + memberId, e);
            throw new RuntimeException("Failed to update phone", e);
        }
    }
    
    @Override
    public boolean updateAddress(Integer memberId, String newAddress) {
        String sql = "UPDATE members SET address = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newAddress);
                    ps.setInt(2, memberId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Member address updated: memberId={0}", memberId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member address: " + memberId, e);
            throw new RuntimeException("Failed to update address", e);
        }
    }
    
    @Override
    public List<Member> searchByName(String searchTerm) {
        String sql = "SELECT * FROM members WHERE " +
                     "LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?) " +
                     "ORDER BY first_name, last_name";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        String pattern = "%" + searchTerm + "%";
                        ps.setString(1, pattern);
                        ps.setString(2, pattern);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                MEMBER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching members by name: " + searchTerm, e);
            throw new RuntimeException("Failed to search members", e);
        }
    }
}
