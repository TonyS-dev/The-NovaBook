/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.codeup.novabook.infra.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application configuration manager that loads and provides access to configuration properties.
 * 
 * <p>This class follows the Singleton pattern for configuration management and loads
 * properties from the classpath resource {@code /application.properties}. It provides
 * a centralized way to access configuration values throughout the application.</p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * AppConfig config = new AppConfig();
 * String dbHost = config.get("db.host");
 * String dbPort = config.get("db.port");
 * }</pre>
 * 
 * <p><strong>Configuration Properties:</strong></p>
 * <ul>
 *   <li>{@code db.vendor} - Database vendor (postgres/mysql)</li>
 *   <li>{@code db.host} - Database host address</li>
 *   <li>{@code db.port} - Database port number</li>
 *   <li>{@code db.name} - Database name</li>
 *   <li>{@code db.user} - Database username</li>
 *   <li>{@code db.password} - Database password</li>
 * </ul>
 * 
 * @version 1.0
 * @since 1.0
 * @see Properties
 * @see InputStream
 */
public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private final Properties props = new Properties();

    /**
     * Constructs a new AppConfig instance and loads configuration properties.
     * 
     * <p>Automatically loads properties from {@code /application.properties} file
     * located in the classpath. The file must be present or an exception will be thrown.</p>
     * 
     * @throws IllegalStateException if the application.properties file is not found
     * @throws RuntimeException if there's an error reading the properties file
     */
    public AppConfig() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found");
            }
            logger.log(Level.INFO, "Loading configuration from application.properties: {0}", in.toString());
            props.load(in);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading configuration: {0}", e.getMessage());
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    // Allows getting any property by key
    /**
     * Returns the value for the given configuration key.
     * @param key the property key
     * @return the property value, or null if not found
     */
    public String get(String key) { return props.getProperty(key); }
}
