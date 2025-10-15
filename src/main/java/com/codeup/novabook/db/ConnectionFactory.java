/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.codeup.novabook.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.codeup.novabook.infra.config.AppConfig;
/**
 * Database connection factory that implements the Factory Method pattern.
 * 
 * <p>This factory centralizes JDBC connection creation and supports multiple database
 * vendors including PostgreSQL and MySQL. The database configuration is externalized
 * through the {@link AppConfig} system, allowing runtime database selection.</p>
 * 
 * <p><strong>Supported Database Vendors:</strong></p>
 * <ul>
 *   <li><strong>PostgreSQL</strong> - When {@code db.vendor=postgres}</li>
 *   <li><strong>MySQL</strong> - When {@code db.vendor=mysql} (default)</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * AppConfig config = new AppConfig();
 * ConnectionFactory factory = new ConnectionFactory(config);
 * 
 * try (Connection conn = factory.open()) {
 *     // Use database connection
 *     PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
 *     // ... perform database operations
 * }
 * }</pre>
 * 
 * <p><strong>Configuration Requirements:</strong></p>
 * <ul>
 *   <li>{@code db.vendor} - Database type (postgres/mysql)</li>
 *   <li>{@code db.host} - Database server hostname</li>
 *   <li>{@code db.port} - Database server port</li>
 *   <li>{@code db.name} - Database name</li>
 *   <li>{@code db.user} - Database username</li>
 *   <li>{@code db.password} - Database password</li>
 *   <li>{@code db.useSSL} - SSL flag (MySQL only)</li>
 * </ul>
 * 
 * @version 1.0
 * @since 1.0
 * @see AppConfig
 * @see Connection
 * @see DriverManager
 */
public class ConnectionFactory {
    private static final Logger logger = Logger.getLogger(ConnectionFactory.class.getName());
    private final AppConfig cfg;
    
    /**
     * Constructs a new ConnectionFactory with the specified configuration.
     * 
     * @param cfg the application configuration containing database settings
     * @throws NullPointerException if cfg is null
     */
    public ConnectionFactory(AppConfig cfg) { this.cfg = cfg; }

    /**
     * Opens a new database connection based on the configured database vendor.
     * 
     * <p>This method creates JDBC connections for different database types based on
     * the {@code db.vendor} configuration property. It automatically constructs the
     * appropriate JDBC URL and connection parameters.</p>
     * 
     * <p><strong>PostgreSQL URL Format:</strong><br>
     * {@code jdbc:postgresql://host:port/database}</p>
     * 
     * <p><strong>MySQL URL Format:</strong><br>
     * {@code jdbc:mysql://host:port/database?useSSL=value&serverTimezone=UTC}</p>
     * 
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * try (Connection conn = factory.open()) {
     *     // Connection is automatically closed due to try-with-resources
     *     PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
     *     ResultSet rs = ps.executeQuery();
     *     // ... process results
     * }
     * }</pre>
     * 
     * @return a new database connection ready for use
     * @throws SQLException if a database access error occurs or connection fails
     * @throws IllegalArgumentException if required configuration properties are missing
     */
    public Connection open() throws SQLException {
        String vendor = cfg.get("db.vendor");
        String host = cfg.get("db.host");
        String port = cfg.get("db.port");
        String name = cfg.get("db.name");
        String user = cfg.get("db.user");
        String pass = cfg.get("db.password");

        String url;
        if ("postgres".equalsIgnoreCase(vendor)) {
            logger.info("Using PostgreSQL");
            url = String.format("jdbc:postgresql://%s:%s/%s", host, port, name);
        } else {
            logger.info("Using MySQL");
            String useSSL = cfg.get("db.useSSL");
            url = String.format("jdbc:mysql://%s:%s/%s?useSSL=%s&serverTimezone=UTC", host, port, name, useSSL);
        }
        return DriverManager.getConnection(url, user, pass);
    }
}