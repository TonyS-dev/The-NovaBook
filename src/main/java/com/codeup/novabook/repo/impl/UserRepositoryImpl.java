package com.codeup.novabook.repo.impl;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.domain.User;
import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.domain.UserStatus;
import com.codeup.novabook.jdbc.JdbcTemplateLight;
import com.codeup.novabook.jdbc.RowMapper;
import com.codeup.novabook.repo.IUserRepository;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of the IUserRepository interface using JdbcTemplateLight.
 * <p>
 * This implementation uses {@link JdbcTemplateLight} to simplify database operations,
 * eliminate repetitive connection management, and provide clean transaction support.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class UserRepositoryImpl implements IUserRepository {
    
    private static final Logger LOGGER = Logger.getLogger(UserRepositoryImpl.class.getName());
    private final JdbcTemplateLight jdbcTemplate;
    
    private static final RowMapper<User> USER_MAPPER = rs -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    };
    
    public UserRepositoryImpl(ConnectionFactory connectionFactory) {
        this.jdbcTemplate = new JdbcTemplateLight(connectionFactory);
    }
    
    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (name, email, password, phone, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        
        try {
            // Use txExecute to handle RETURNING clause properly
            return jdbcTemplate.txExecute(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getPhone());
                    ps.setString(5, user.getRole().name());
                    ps.setString(6, user.getStatus().name());
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        user.setId(rs.getInt("id"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    
                    LOGGER.log(Level.INFO, "User created: {0}", user.getEmail());
                    return user;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getEmail(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try {
            List<User> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                USER_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + id, e);
            throw new RuntimeException("Failed to find user", e);
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try {
            return jdbcTemplate.query(sql, null, USER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all users", e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }
    
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, phone = ?, " +
                     "role = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getPhone());
                    ps.setString(5, user.getRole().name());
                    ps.setString(6, user.getStatus().name());
                    ps.setInt(7, user.getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("User not found: " + user.getId());
            }
            
            LOGGER.log(Level.INFO, "User updated: {0}", user.getEmail());
            return user;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user.getId(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        // Soft delete: update status to DELETED instead of removing the record
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, UserStatus.DELETED.name());
                    ps.setInt(2, id);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("User not found: " + id);
            }
            
            LOGGER.log(Level.INFO, "User soft deleted: {0}", id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: " + id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try {
            List<User> results = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, email);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                USER_MAPPER);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email: " + email, e);
            throw new RuntimeException("Failed to find user", e);
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
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
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, role.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                USER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by role: " + role, e);
            throw new RuntimeException("Failed to find users", e);
        }
    }
    
    @Override
    public List<User> findByStatus(UserStatus status) {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY name";
        
        try {
            return jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, status.name());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                USER_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding users by status: " + status, e);
            throw new RuntimeException("Failed to find users", e);
        }
    }
    
    @Override
    public boolean updateStatus(Integer userId, UserStatus status) {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, status.name());
                    ps.setInt(2, userId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User status updated: userId={0}, status={1}", 
                          new Object[]{userId, status});
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user status: " + userId, e);
            throw new RuntimeException("Failed to update status", e);
        }
    }
    
    @Override
    public boolean updatePassword(Integer userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, hashedPassword);
                    ps.setInt(2, userId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User password updated: userId={0}", userId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user password: " + userId, e);
            throw new RuntimeException("Failed to update password", e);
        }
    }
    
    @Override
    public boolean updateEmail(Integer userId, String newEmail) {
        String sql = "UPDATE users SET email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newEmail);
                    ps.setInt(2, userId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User email updated: userId={0}, newEmail={1}", 
                          new Object[]{userId, newEmail});
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user email: " + userId, e);
            throw new RuntimeException("Failed to update email", e);
        }
    }
    
    @Override
    public boolean updatePhone(Integer userId, String newPhone) {
        String sql = "UPDATE users SET phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newPhone);
                    ps.setInt(2, userId);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "User phone updated: userId={0}", userId);
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user phone: " + userId, e);
            throw new RuntimeException("Failed to update phone", e);
        }
    }
}
