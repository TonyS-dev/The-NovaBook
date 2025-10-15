package com.codeup.novabook.repo;

import java.util.List;
import java.util.Optional;

import com.codeup.novabook.domain.SystemConfig;

/**
 * Repository interface for SystemConfig entity operations.
 * <p>
 * Manages system-wide configuration parameters stored in the database.
 * Provides key-based retrieval and update operations for runtime configuration.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Configuration CRUD operations</li>
 *   <li>Key-based configuration lookup</li>
 *   <li>Editable configuration filtering</li>
 *   <li>Typed value retrieval (integer, decimal, boolean)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IConfigRepository configRepo = new ConfigRepositoryImpl();
 * 
 * // Get configuration by key
 * Optional<SystemConfig> config = configRepo.findByKey("DEFAULT_LOAN_DAYS");
 * int loanDays = config.map(SystemConfig::getAsInteger).orElse(14);
 * 
 * // Update configuration value
 * configRepo.updateValue("DAILY_FINE_AMOUNT", "2000.00");
 * 
 * // Get all editable configs
 * List<SystemConfig> editableConfigs = configRepo.findAllEditable();
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see SystemConfig
 * @see IGeneralRepository
 */
public interface IConfigRepository extends IGeneralRepository<SystemConfig, Integer> {
    
    /**
     * Finds a configuration parameter by its unique key.
     * 
     * @param configKey the configuration key (e.g., "DEFAULT_LOAN_DAYS")
     * @return Optional containing the configuration if found
     */
    Optional<SystemConfig> findByKey(String configKey);
    
    /**
     * Retrieves all editable configuration parameters.
     * <p>
     * Editable configurations can be modified through the admin UI.
     * Non-editable configs (like SYSTEM_NAME) are for display only.
     * </p>
     * 
     * @return list of editable system configurations
     */
    List<SystemConfig> findAllEditable();
    
    /**
     * Updates a configuration value by its key.
     * <p>
     * This is a convenience method that updates only the config_value field.
     * The updated_at timestamp is automatically updated by database trigger.
     * </p>
     * 
     * @param configKey the configuration key to update
     * @param newValue the new configuration value
     * @return the number of rows affected (should be 1 if successful)
     * @throws RuntimeException if update fails
     */
    int updateValue(String configKey, String newValue);
    
    /**
     * Checks if a configuration key exists in the database.
     * 
     * @param configKey the configuration key to check
     * @return true if the key exists, false otherwise
     */
    boolean existsByKey(String configKey);
}
