package com.codeup.novabook.repo.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.domain.ConfigDataType;
import com.codeup.novabook.domain.SystemConfig;
import com.codeup.novabook.jdbc.JdbcTemplateLight;
import com.codeup.novabook.jdbc.RowMapper;
import com.codeup.novabook.repo.IConfigRepository;

/**
 * JDBC implementation of the IConfigRepository interface using JdbcTemplateLight.
 * <p>
 * This repository manages {@link SystemConfig} entities for system-wide configuration
 * parameters. Provides key-based retrieval and update operations for runtime configuration.
 * </p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Key-based configuration lookup</li>
 *   <li>Editable configuration filtering</li>
 *   <li>Atomic configuration value updates</li>
 *   <li>Type-safe configuration retrieval</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see IConfigRepository
 * @see SystemConfig
 */
public class ConfigRepositoryImpl implements IConfigRepository {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigRepositoryImpl.class.getName());
    private final JdbcTemplateLight jdbcTemplate;
    
    /**
     * RowMapper for converting ResultSet to SystemConfig entity.
     */
    private static final RowMapper<SystemConfig> CONFIG_MAPPER = rs -> {
        SystemConfig config = new SystemConfig();
        config.setId(rs.getInt("id"));
        config.setConfigKey(rs.getString("config_key"));
        config.setConfigValue(rs.getString("config_value"));
        
        // Convert database ENUM value to ConfigDataType
        String dataTypeValue = rs.getString("data_type");
        if (dataTypeValue != null) {
            config.setDataType(ConfigDataType.fromDatabaseValue(dataTypeValue));
        }
        
        config.setDescription(rs.getString("description"));
        config.setEditable(rs.getBoolean("is_editable"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            config.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            config.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return config;
    };
    
    public ConfigRepositoryImpl(ConnectionFactory connectionFactory) {
        this.jdbcTemplate = new JdbcTemplateLight(connectionFactory);
    }
    
    @Override
    public SystemConfig create(SystemConfig config) {
        String sql = "INSERT INTO system_config (config_key, config_value, data_type, description, is_editable) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try {
            return jdbcTemplate.txExecute(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, config.getConfigKey());
                    ps.setString(2, config.getConfigValue());
                    ps.setString(3, config.getDataType().toDatabaseValue());
                    ps.setString(4, config.getDescription());
                    ps.setBoolean(5, config.getEditable());
                    
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        config.setId(rs.getInt("id"));
                    }
                    
                    LOGGER.log(Level.INFO, "Config created: {0}", config.getConfigKey());
                    return config;
                }
            });
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating config: " + config.getConfigKey(), e);
            throw new RuntimeException("Failed to create config", e);
        }
    }
    
    @Override
    public Optional<SystemConfig> findById(Integer id) {
        String sql = "SELECT * FROM system_config WHERE id = ?";
        
        try {
            List<SystemConfig> configs = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setInt(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                CONFIG_MAPPER);
            
            return configs.isEmpty() ? Optional.empty() : Optional.of(configs.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding config by ID: " + id, e);
            throw new RuntimeException("Failed to find config", e);
        }
    }
    
    @Override
    public Optional<SystemConfig> findByKey(String configKey) {
        String sql = "SELECT * FROM system_config WHERE config_key = ?";
        
        try {
            List<SystemConfig> configs = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, configKey);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                CONFIG_MAPPER);
            
            return configs.isEmpty() ? Optional.empty() : Optional.of(configs.get(0));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding config by key: " + configKey, e);
            throw new RuntimeException("Failed to find config", e);
        }
    }
    
    @Override
    public List<SystemConfig> findAll() {
        String sql = "SELECT * FROM system_config ORDER BY config_key";
        
        try {
            return jdbcTemplate.query(sql, ps -> {}, CONFIG_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all configs", e);
            throw new RuntimeException("Failed to find configs", e);
        }
    }
    
    @Override
    public List<SystemConfig> findAllEditable() {
        String sql = "SELECT * FROM system_config WHERE is_editable = true ORDER BY config_key";
        
        try {
            return jdbcTemplate.query(sql, ps -> {}, CONFIG_MAPPER);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding editable configs", e);
            throw new RuntimeException("Failed to find editable configs", e);
        }
    }
    
    @Override
    public SystemConfig update(SystemConfig config) {
        String sql = "UPDATE system_config SET config_value = ?, data_type = ?, " +
                     "description = ?, is_editable = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, config.getConfigValue());
                    ps.setString(2, config.getDataType().toDatabaseValue());
                    ps.setString(3, config.getDescription());
                    ps.setBoolean(4, config.getEditable());
                    ps.setInt(5, config.getId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Config not found with ID: " + config.getId());
            }
            
            LOGGER.log(Level.INFO, "Config updated: {0}", config.getConfigKey());
            return config;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating config: " + config.getConfigKey(), e);
            throw new RuntimeException("Failed to update config", e);
        }
    }
    
    @Override
    public int updateValue(String configKey, String newValue) {
        String sql = "UPDATE system_config SET config_value = ? WHERE config_key = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setString(1, newValue);
                    ps.setString(2, configKey);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Config value updated: {0} = {1}", 
                          new Object[]{configKey, newValue});
            }
            
            return rowsAffected;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating config value: " + configKey, e);
            throw new RuntimeException("Failed to update config value", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM system_config WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, ps -> {
                try {
                    ps.setInt(1, id);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO, "Config deleted with ID: {0}", id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting config: " + id, e);
            throw new RuntimeException("Failed to delete config", e);
        }
    }
    
    @Override
    public boolean existsByKey(String configKey) {
        String sql = "SELECT COUNT(*) as count FROM system_config WHERE config_key = ?";
        
        try {
            List<Integer> result = jdbcTemplate.query(sql, 
                ps -> {
                    try {
                        ps.setString(1, configKey);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 
                rs -> rs.getInt("count"));
            
            return !result.isEmpty() && result.get(0) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if config exists: " + configKey, e);
            throw new RuntimeException("Failed to check config existence", e);
        }
    }
}
