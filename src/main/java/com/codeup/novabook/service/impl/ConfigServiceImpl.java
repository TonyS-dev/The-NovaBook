package com.codeup.novabook.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.domain.ConfigDataType;
import com.codeup.novabook.domain.SystemConfig;
import com.codeup.novabook.repo.IConfigRepository;
import com.codeup.novabook.service.IConfigService;

/**
 * Implementation of IConfigService for system configuration management.
 * <p>
 * Provides business logic for retrieving and updating system-wide configuration
 * parameters. Uses caching for frequently accessed configuration values.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see IConfigService
 * @see SystemConfig
 */
public class ConfigServiceImpl implements IConfigService {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigServiceImpl.class.getName());
    
    private final IConfigRepository configRepository;
    private final Map<String, SystemConfig> configCache;
    
    // Configuration keys constants
    private static final String KEY_DEFAULT_LOAN_DAYS = "DEFAULT_LOAN_DAYS";
    private static final String KEY_DAILY_FINE_AMOUNT = "DAILY_FINE_AMOUNT";
    private static final String KEY_MAX_ACTIVE_LOANS = "MAX_ACTIVE_LOANS";
    private static final String KEY_MAX_LOAN_EXTENSION_DAYS = "MAX_LOAN_EXTENSION_DAYS";
    private static final String KEY_LOAN_DUE_REMINDER_DAYS = "LOAN_DUE_REMINDER_DAYS";
    private static final String KEY_SYSTEM_NAME = "SYSTEM_NAME";
    private static final String KEY_CURRENCY_SYMBOL = "CURRENCY_SYMBOL";
    private static final String KEY_ALLOW_LOAN_EXTENSIONS = "ALLOW_LOAN_EXTENSIONS";
    private static final String KEY_AUTO_SUSPEND_ON_OVERDUE = "AUTO_SUSPEND_ON_OVERDUE";
    private static final String KEY_NOTIFICATION_EMAIL_ENABLED = "NOTIFICATION_EMAIL_ENABLED";
    
    // Default values (fallback if DB value not found)
    private static final int DEFAULT_LOAN_DAYS_VALUE = 14;
    private static final BigDecimal DEFAULT_DAILY_FINE = new BigDecimal("1500.00");
    private static final int DEFAULT_MAX_ACTIVE_LOANS = 3;
    private static final int DEFAULT_MAX_EXTENSION_DAYS = 7;
    private static final int DEFAULT_REMINDER_DAYS = 3;
    private static final String DEFAULT_SYSTEM_NAME = "NovaBook Library Management";
    private static final String DEFAULT_CURRENCY_SYMBOL = "$";
    
    public ConfigServiceImpl(IConfigRepository configRepository) {
        this.configRepository = configRepository;
        this.configCache = new HashMap<>();
        loadCache();
    }
    
    /**
     * Loads all configurations into cache on initialization.
     */
    private void loadCache() {
        try {
            List<SystemConfig> allConfigs = configRepository.findAll();
            for (SystemConfig config : allConfigs) {
                configCache.put(config.getConfigKey(), config);
            }
            LOGGER.log(Level.INFO, "Loaded {0} configurations into cache", allConfigs.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load configurations into cache", e);
        }
    }
    
    @Override
    public void reloadCache() {
        configCache.clear();
        loadCache();
    }
    
    /**
     * Gets a configuration from cache or database.
     */
    private SystemConfig getConfig(String key) {
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        
        // Try to load from database
        Optional<SystemConfig> config = configRepository.findByKey(key);
        if (config.isPresent()) {
            configCache.put(key, config.get());
            return config.get();
        }
        
        return null;
    }
    
    @Override
    public int getDefaultLoanDays() {
        SystemConfig config = getConfig(KEY_DEFAULT_LOAN_DAYS);
        if (config != null) {
            try {
                return config.getAsInteger();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing DEFAULT_LOAN_DAYS, using default", e);
            }
        }
        return DEFAULT_LOAN_DAYS_VALUE;
    }
    
    @Override
    public BigDecimal getDailyFineAmount() {
        SystemConfig config = getConfig(KEY_DAILY_FINE_AMOUNT);
        if (config != null) {
            try {
                return config.getAsDecimal();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing DAILY_FINE_AMOUNT, using default", e);
            }
        }
        return DEFAULT_DAILY_FINE;
    }
    
    @Override
    public int getMaxActiveLoans() {
        SystemConfig config = getConfig(KEY_MAX_ACTIVE_LOANS);
        if (config != null) {
            try {
                return config.getAsInteger();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing MAX_ACTIVE_LOANS, using default", e);
            }
        }
        return DEFAULT_MAX_ACTIVE_LOANS;
    }
    
    @Override
    public int getMaxLoanExtensionDays() {
        SystemConfig config = getConfig(KEY_MAX_LOAN_EXTENSION_DAYS);
        if (config != null) {
            try {
                return config.getAsInteger();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing MAX_LOAN_EXTENSION_DAYS, using default", e);
            }
        }
        return DEFAULT_MAX_EXTENSION_DAYS;
    }
    
    @Override
    public int getLoanDueReminderDays() {
        SystemConfig config = getConfig(KEY_LOAN_DUE_REMINDER_DAYS);
        if (config != null) {
            try {
                return config.getAsInteger();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing LOAN_DUE_REMINDER_DAYS, using default", e);
            }
        }
        return DEFAULT_REMINDER_DAYS;
    }
    
    @Override
    public String getSystemName() {
        SystemConfig config = getConfig(KEY_SYSTEM_NAME);
        return config != null ? config.getAsString() : DEFAULT_SYSTEM_NAME;
    }
    
    @Override
    public String getCurrencySymbol() {
        SystemConfig config = getConfig(KEY_CURRENCY_SYMBOL);
        return config != null ? config.getAsString() : DEFAULT_CURRENCY_SYMBOL;
    }
    
    @Override
    public boolean isLoanExtensionsAllowed() {
        SystemConfig config = getConfig(KEY_ALLOW_LOAN_EXTENSIONS);
        if (config != null) {
            try {
                return config.getAsBoolean();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing ALLOW_LOAN_EXTENSIONS, using default", e);
            }
        }
        return true; // Default to true
    }
    
    @Override
    public boolean isAutoSuspendOnOverdueEnabled() {
        SystemConfig config = getConfig(KEY_AUTO_SUSPEND_ON_OVERDUE);
        if (config != null) {
            try {
                return config.getAsBoolean();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing AUTO_SUSPEND_ON_OVERDUE, using default", e);
            }
        }
        return false; // Default to false
    }
    
    @Override
    public boolean isNotificationEmailEnabled() {
        SystemConfig config = getConfig(KEY_NOTIFICATION_EMAIL_ENABLED);
        if (config != null) {
            try {
                return config.getAsBoolean();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error parsing NOTIFICATION_EMAIL_ENABLED, using default", e);
            }
        }
        return false; // Default to false
    }
    
    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return getConfig(configKey);
    }
    
    @Override
    public void updateConfiguration(String configKey, String newValue) {
        // Get the configuration
        SystemConfig config = getConfig(configKey);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + configKey);
        }
        
        // Check if editable
        if (!config.getEditable()) {
            throw new IllegalArgumentException("Configuration is not editable: " + configKey);
        }
        
        // Validate the new value
        if (!validateConfigValue(configKey, newValue)) {
            throw new IllegalArgumentException("Invalid value for configuration: " + configKey);
        }
        
        // Update in database
        int rowsAffected = configRepository.updateValue(configKey, newValue);
        if (rowsAffected == 0) {
            throw new RuntimeException("Failed to update configuration: " + configKey);
        }
        
        // Update cache
        config.setConfigValue(newValue);
        configCache.put(configKey, config);
        
        LOGGER.log(Level.INFO, "Configuration updated: {0} = {1}", 
                  new Object[]{configKey, newValue});
    }
    
    @Override
    public Map<String, String> getAllEditableConfigs() {
        Map<String, String> configs = new HashMap<>();
        List<SystemConfig> editableConfigs = configRepository.findAllEditable();
        
        for (SystemConfig config : editableConfigs) {
            configs.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return configs;
    }
    
    @Override
    public Map<String, SystemConfig> getAllEditableConfigObjects() {
        Map<String, SystemConfig> configs = new HashMap<>();
        List<SystemConfig> editableConfigs = configRepository.findAllEditable();
        
        for (SystemConfig config : editableConfigs) {
            configs.put(config.getConfigKey(), config);
        }
        
        return configs;
    }
    
    @Override
    public boolean validateConfigValue(String configKey, String value) {
        SystemConfig config = getConfig(configKey);
        if (config == null) {
            return false;
        }
        
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // Validate based on data type
        ConfigDataType dataType = config.getDataType();
        
        try {
            switch (dataType) {
                case INTEGER -> {
                    Integer intValue = Integer.valueOf(value);
                    // Additional validation for specific configs
                    if (configKey.equals(KEY_DEFAULT_LOAN_DAYS) && intValue < 1) {
                        return false;
                    }
                    return !(configKey.equals(KEY_MAX_ACTIVE_LOANS) && intValue < 1);
                }

                    
                case DECIMAL -> {
                    BigDecimal decimalValue = new BigDecimal(value);
                    // Ensure non-negative for fine amounts
                    return !(configKey.equals(KEY_DAILY_FINE_AMOUNT) && decimalValue.compareTo(BigDecimal.ZERO) < 0);
                }

                    
                case BOOLEAN -> {
                    String lowerValue = value.toLowerCase().trim();
                    return lowerValue.equals("true") || lowerValue.equals("false");
                }
                    
                case STRING -> {
                    return !value.trim().isEmpty();
                }
                    
                default -> {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Validation failed for {0} = {1}", 
                      new Object[]{configKey, value});
            return false;
        }
    }
}
