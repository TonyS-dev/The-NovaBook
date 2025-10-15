package com.codeup.novabook.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SystemConfig entity representing system-wide configuration parameters.
 * <p>
 * This class encapsulates configuration settings that control system behavior
 * such as loan duration, fine amounts, and feature toggles. Values are stored
 * as strings in the database with type information to enable proper parsing.
 * </p>
 * 
 * <p><b>Supported Data Types:</b></p>
 * <ul>
 *   <li>STRING - Text values</li>
 *   <li>INTEGER - Whole numbers</li>
 *   <li>DECIMAL - Decimal numbers (for prices/fines)</li>
 *   <li>BOOLEAN - true/false values</li>
 * </ul>
 * 
 * <p><b>Example configuration keys:</b></p>
 * <ul>
 *   <li>DEFAULT_LOAN_DAYS - Default loan duration (INTEGER)</li>
 *   <li>DAILY_FINE_AMOUNT - Fine per overdue day (DECIMAL)</li>
 *   <li>MAX_ACTIVE_LOANS - Maximum simultaneous loans (INTEGER)</li>
 *   <li>SYSTEM_NAME - Application name (STRING)</li>
 *   <li>ALLOW_LOAN_EXTENSIONS - Feature toggle (BOOLEAN)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * SystemConfig config = new SystemConfig();
 * config.setConfigKey("DEFAULT_LOAN_DAYS");
 * config.setConfigValue("14");
 * config.setDataType(ConfigDataType.INTEGER);
 * config.setDescription("Default number of days for a loan");
 * config.setEditable(true);
 * 
 * // Get typed value
 * int loanDays = config.getAsInteger();
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see ConfigDataType
 */
public class SystemConfig {
    
    private Integer id;
    private String configKey; // e.g., "DEFAULT_LOAN_DAYS"
    private String configValue; // Stored as string, parsed based on dataType
    private ConfigDataType dataType; // STRING, INTEGER, DECIMAL, BOOLEAN
    private String description;
    private Boolean editable; // Whether admins can edit via UI
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Default constructor for SystemConfig.
     */
    public SystemConfig() {
        this.editable = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new SystemConfig with essential information.
     * 
     * @param configKey the unique configuration key
     * @param configValue the configuration value as string
     * @param dataType the data type for parsing
     */
    public SystemConfig(String configKey, String configValue, ConfigDataType dataType) {
        this();
        this.configKey = configKey;
        this.configValue = configValue;
        this.dataType = dataType;
    }

    /**
     * Gets the configuration value as an Integer.
     * 
     * @return the parsed integer value
     * @throws IllegalStateException if dataType is not INTEGER
     * @throws NumberFormatException if value cannot be parsed
     */
    public Integer getAsInteger() {
        if (dataType != ConfigDataType.INTEGER) {
            throw new IllegalStateException("Config " + configKey + " is not of type INTEGER");
        }
        return Integer.valueOf(configValue);
    }

    /**
     * Gets the configuration value as a BigDecimal.
     * 
     * @return the parsed decimal value
     * @throws IllegalStateException if dataType is not DECIMAL
     * @throws NumberFormatException if value cannot be parsed
     */
    public BigDecimal getAsDecimal() {
        if (dataType != ConfigDataType.DECIMAL) {
            throw new IllegalStateException("Config " + configKey + " is not of type DECIMAL");
        }
        return new BigDecimal(configValue);
    }

    /**
     * Gets the configuration value as a Boolean.
     * 
     * @return the parsed boolean value
     * @throws IllegalStateException if dataType is not BOOLEAN
     */
    public Boolean getAsBoolean() {
        if (dataType != ConfigDataType.BOOLEAN) {
            throw new IllegalStateException("Config " + configKey + " is not of type BOOLEAN");
        }
        return Boolean.valueOf(configValue);
    }

    /**
     * Gets the configuration value as a String.
     * 
     * @return the string value
     */
    public String getAsString() {
        return configValue;
    }

    // ============== GETTERS AND SETTERS ==============

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public ConfigDataType getDataType() {
        return dataType;
    }

    public void setDataType(ConfigDataType dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
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

    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", dataType=" + dataType +
                ", description='" + description + '\'' +
                ", editable=" + editable +
                '}';
    }
}
