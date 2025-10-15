/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.codeup.novabook.jdbc;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional interface for mapping database result set rows to domain objects.
 * <p>
 * This interface defines a contract for converting {@link ResultSet} rows
 * into domain objects. It's used in conjunction with {@link JdbcTemplateLight}
 * to provide type-safe database query results.
 * </p>
 * <p>Example usage:</p>
 * <pre>{@code
 * RowMapper<User> userMapper = rs -> {
 *     User user = new User(
 *         rs.getString("name"),
 *         rs.getString("email"),
 *         rs.getString("password"),
 *         rs.getString("phone")
 *     );
 *     user.setId(rs.getObject("id", UUID.class));
 *     return user;
 * };
 * 
 * List<User> users = jdbcTemplate.query("SELECT * FROM users", null, userMapper);
 * }</pre>
 * 
 * @param <T> the type of object that this mapper produces
 * @version 1.0
 * @since 1.0
 * @see JdbcTemplateLight
 * @see java.sql.ResultSet
 */
@FunctionalInterface
public interface RowMapper<T> {
    
    /**
     * Maps a single row of the {@link ResultSet} to an object of type T.
     * <p>
     * This method is called for each row in the result set. The ResultSet
     * is positioned at the current row when this method is called.
     * </p>
     * 
     * @param rs the ResultSet positioned at the current row
     * @return an object of type T representing the current row
     * @throws SQLException if a database access error occurs or the column 
     *                      labels/indices are invalid
     */
    T map(ResultSet rs) throws SQLException;
}
