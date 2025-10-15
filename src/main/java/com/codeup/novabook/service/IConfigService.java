package com.codeup.novabook.service;

import java.math.BigDecimal;
import java.util.Map;

import com.codeup.novabook.domain.SystemConfig;

/**
 * Service interface for system configuration management.
 * <p>
 * Provides business logic for retrieving and updating system-wide configuration
 * parameters such as loan duration, fine amounts, and feature toggles.
 * </p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Type-safe configuration value retrieval</li>
 *   <li>Configuration validation and updates</li>
 *   <li>Default value handling</li>
 *   <li>Configuration caching for performance</li>
 * </ul>
 * 
 * <p><b>Default Configuration Keys:</b></p>
 * <ul>
 *   <li>DEFAULT_LOAN_DAYS - Default loan duration (14 days)</li>
 *   <li>DAILY_FINE_AMOUNT - Fine per overdue day ($1500)</li>
 *   <li>MAX_ACTIVE_LOANS - Maximum simultaneous loans (3)</li>
 *   <li>MAX_LOAN_EXTENSION_DAYS - Maximum extension days (7)</li>
 *   <li>LOAN_DUE_REMINDER_DAYS - Days before due date for reminder (3)</li>
 *   <li>SYSTEM_NAME - Application name</li>
 *   <li>CURRENCY_SYMBOL - Currency symbol ($)</li>
 *   <li>ALLOW_LOAN_EXTENSIONS - Feature toggle</li>
 *   <li>AUTO_SUSPEND_ON_OVERDUE - Auto-suspend members with overdue loans</li>
 *   <li>NOTIFICATION_EMAIL_ENABLED - Enable email notifications</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IConfigService configService = new ConfigServiceImpl(configRepo);
 * 
 * // Get configuration values
 * int loanDays = configService.getDefaultLoanDays();
 * BigDecimal fineAmount = configService.getDailyFineAmount();
 * 
 * // Update configuration
 * configService.updateConfiguration("DEFAULT_LOAN_DAYS", "21");
 * 
 * // Get all editable configs for UI
 * Map<String, String> configs = configService.getAllEditableConfigs();
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see SystemConfig
 */
public interface IConfigService {
    
    /**
     * Gets the default loan duration in days.
     * 
     * @return the default number of days for a loan (default: 14)
     */
    int getDefaultLoanDays();
    
    /**
     * Gets the daily fine amount for overdue loans.
     * 
     * @return the fine amount per day (default: 1500.00)
     */
    BigDecimal getDailyFineAmount();
    
    /**
     * Gets the maximum number of active loans per member.
     * 
     * @return the maximum active loans (default: 3)
     */
    int getMaxActiveLoans();
    
    /**
     * Gets the maximum number of days a loan can be extended.
     * 
     * @return the maximum extension days (default: 7)
     */
    int getMaxLoanExtensionDays();
    
    /**
     * Gets the number of days before due date to send reminder.
     * 
     * @return the reminder days (default: 3)
     */
    int getLoanDueReminderDays();
    
    /**
     * Gets the system name.
     * 
     * @return the system name (default: "NovaBook Library Management")
     */
    String getSystemName();
    
    /**
     * Gets the currency symbol.
     * 
     * @return the currency symbol (default: "$")
     */
    String getCurrencySymbol();
    
    /**
     * Checks if loan extensions are allowed.
     * 
     * @return true if extensions are allowed (default: true)
     */
    boolean isLoanExtensionsAllowed();
    
    /**
     * Checks if members should be auto-suspended on overdue loans.
     * 
     * @return true if auto-suspend is enabled (default: false)
     */
    boolean isAutoSuspendOnOverdueEnabled();
    
    /**
     * Checks if email notifications are enabled.
     * 
     * @return true if notifications are enabled (default: false)
     */
    boolean isNotificationEmailEnabled();
    
    /**
     * Retrieves a configuration value by its key.
     * 
     * @param configKey the configuration key
     * @return the SystemConfig object, or null if not found
     */
    SystemConfig getConfigByKey(String configKey);
    
    /**
     * Updates a configuration value.
     * <p>
     * Validates the new value based on the configuration's data type
     * before updating. Only editable configurations can be updated.
     * </p>
     * 
     * @param configKey the configuration key to update
     * @param newValue the new configuration value
     * @throws IllegalArgumentException if config is not editable or value is invalid
     * @throws RuntimeException if update fails
     */
    void updateConfiguration(String configKey, String newValue);
    
    /**
     * Retrieves all editable configuration parameters as key-value pairs.
     * <p>
     * Used for populating the configuration UI in the admin dashboard.
     * </p>
     * 
     * @return map of configuration keys to their current values
     */
    Map<String, String> getAllEditableConfigs();
    
    /**
     * Retrieves all editable configuration objects with full details.
     * <p>
     * Includes data types, descriptions, and editability flags.
     * </p>
     * 
     * @return map of configuration keys to SystemConfig objects
     */
    Map<String, SystemConfig> getAllEditableConfigObjects();
    
    /**
     * Validates a configuration value based on its data type.
     * 
     * @param configKey the configuration key
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    boolean validateConfigValue(String configKey, String value);
    
    /**
     * Reloads configuration cache from database.
     * <p>
     * Should be called after configuration updates to ensure
     * the service reflects the latest database values.
     * </p>
     */
    void reloadCache();
}
