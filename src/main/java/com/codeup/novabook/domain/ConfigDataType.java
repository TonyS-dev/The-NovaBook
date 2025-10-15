package com.codeup.novabook.domain;

/**
 * Enumeration of supported configuration data types.
 * <p>
 * This enum defines the types that configuration values can be parsed to.
 * All values are stored as strings in the database, and this type determines
 * how they should be interpreted and validated.
 * </p>
 * 
 * <p><b>Supported Types:</b></p>
 * <ul>
 *   <li><b>STRING:</b> Text values (e.g., system name, currency symbol)</li>
 *   <li><b>INTEGER:</b> Whole numbers (e.g., max loans, loan days)</li>
 *   <li><b>DECIMAL:</b> Decimal numbers (e.g., fine amounts, prices)</li>
 *   <li><b>BOOLEAN:</b> true/false values (e.g., feature toggles)</li>
 * </ul>
 * 
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * ConfigDataType type = ConfigDataType.INTEGER;
 * String value = "14";
 * int parsed = Integer.parseInt(value); // Type indicates how to parse
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see SystemConfig
 */
public enum ConfigDataType {
    /**
     * Text string value.
     */
    STRING,
    
    /**
     * Integer number value.
     */
    INTEGER,
    
    /**
     * Decimal number value (for money/prices).
     */
    DECIMAL,
    
    /**
     * Boolean true/false value.
     */
    BOOLEAN;
    
    /**
     * Converts database string value to ConfigDataType enum.
     * 
     * @param dbValue the database value
     * @return the matching ConfigDataType
     * @throws IllegalArgumentException if dbValue is invalid
     */
    public static ConfigDataType fromDatabaseValue(String dbValue) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Database value cannot be null or empty");
        }
        
        try {
            return ConfigDataType.valueOf(dbValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid config data type: " + dbValue, e);
        }
    }
    
    /**
     * Converts ConfigDataType to database value.
     * 
     * @return the database string value
     */
    public String toDatabaseValue() {
        return this.name();
    }
}
