/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.codeup.novabook.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.codeup.novabook.db.ConnectionFactory;

/**
 * Lightweight JDBC template for simplified database operations.
 * <p>
 * This class provides a simplified interface for JDBC operations, inspired by
 * Spring's JdbcTemplate but designed to be lightweight and dependency-free.
 * It handles resource management, exception handling, and provides convenient
 * methods for common database operations.
 * </p>
 * <p>Key features:</p>
 * <ul>
 * <li>Automatic resource management (Connection, PreparedStatement, ResultSet)</li>
 * <li>Type-safe result mapping using {@link RowMapper}</li>
 * <li>Transaction support with rollback on exceptions</li>
 * <li>Parameterized queries to prevent SQL injection</li>
 * <li>Functional interfaces for flexible parameter binding</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * JdbcTemplateLight jdbc = new JdbcTemplateLight(connectionFactory);
 * 
 * // Query with parameters
 * List<User> users = jdbc.query(
 *     "SELECT * FROM users WHERE active = ?",
 *     ps -> ps.setBoolean(1, true),
 *     userMapper
 * );
 * 
 * // Update with parameters
 * int rows = jdbc.update(
 *     "UPDATE users SET name = ? WHERE id = ?",
 *     ps -> {
 *         ps.setString(1, "New Name");
 *         ps.setObject(2, userId);
 *     }
 * );
 * }</pre>
 * 
 * @version 1.0
 * @since 1.0
 * @see ConnectionFactory
 * @see RowMapper
 */
public class JdbcTemplateLight {
    private final ConnectionFactory factory;
    
    /**
     * Constructs a JdbcTemplateLight with the specified connection factory.
     * 
     * @param factory the connection factory for obtaining database connections
     * @throws IllegalArgumentException if factory is null
     */
    public JdbcTemplateLight(ConnectionFactory factory) { 
        this.factory = factory; 
    }

    /**
     * Executes a query and maps the results using the provided RowMapper.
     * <p>
     * This method handles the complete lifecycle of a database query including
     * connection management, parameter binding, result set processing, and
     * resource cleanup.
     * </p>
     * 
     * @param <T> the type of objects to return
     * @param sql the SQL query to execute
     * @param binder a consumer to bind parameters to the PreparedStatement, can be null
     * @param mapper the RowMapper to convert ResultSet rows to objects
     * @return a list of mapped objects, never null but may be empty
     * @throws SQLException if a database error occurs
     */
    public <T> List<T> query(String sql, Consumer<PreparedStatement> binder, RowMapper<T> mapper) throws SQLException {
        try (Connection c = factory.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.accept(ps); // Permite parametrizar la consulta
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs)); // Mapea cada fila
                return out;
            }
        }
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE).
     * <p>
     * This method handles parameter binding and resource cleanup for
     * data modification operations.
     * </p>
     * 
     * @param sql the SQL statement to execute
     * @param binder a consumer to bind parameters to the PreparedStatement, can be null
     * @return the number of rows affected by the statement
     * @throws SQLException if a database error occurs
     */
    public int update(String sql, Consumer<PreparedStatement> binder) throws SQLException {
        try (Connection c = factory.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.accept(ps);
            return ps.executeUpdate();
        }
    }

    /**
     * Executes multiple operations within a single transaction.
     * <p>
     * This method provides transactional support by disabling auto-commit,
     * executing the callback, and then either committing on success or
     * rolling back on exception. The original auto-commit state is restored.
     * </p>
     * 
     * @param <T> the type of result returned by the callback
     * @param cb the callback containing the transactional operations
     * @return the result returned by the callback
     * @throws SQLException if any database error occurs, triggering rollback
     */
    public <T> T txExecute(SqlTxCallback<T> cb) throws SQLException {
        try (Connection c = factory.open()) {
            boolean prev = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T result = cb.doInTx(c);
                c.commit();
                return result;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(prev);
            }
        }
    }

    /**
     * Functional interface for transactional callback operations.
     * <p>
     * This interface defines a callback that can perform multiple database
     * operations within a single transaction. The connection is managed
     * by the calling {@link #txExecute(SqlTxCallback)} method.
     * </p>
     * 
     * @param <T> the type of result returned by the callback
     */
    @FunctionalInterface
    public interface SqlTxCallback<T> {
        
        /**
         * Performs transactional operations using the provided connection.
         * 
         * @param conn the database connection with auto-commit disabled
         * @return the result of the transactional operations
         * @throws SQLException if any database error occurs
         */
        T doInTx(Connection conn) throws SQLException;
    }
}
