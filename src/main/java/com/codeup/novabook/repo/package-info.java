/**
 * Repository layer providing data access abstractions and implementations.
 * <p>
 * This package contains the data access layer of the EcoFleet system, following
 * the Repository pattern to abstract data persistence operations:
 * </p>
 * <ul>
 * <li><strong>Interfaces:</strong> Define contracts for data access operations</li>
 * <li><strong>JDBC Implementations:</strong> Concrete implementations using JDBC for PostgreSQL</li>
 * </ul>
 * 
 * <p><strong>Repository Interfaces:</strong></p>
 * <ul>
 * <li>{@link com.codeup.novabook.repo.IUserRepository} - User data operations</li>
 * <li>{@link com.codeup.novabook.repo.IVehicleRepository} - Vehicle data operations</li>
 * <li>{@link com.codeup.novabook.repo.IRentalRepository} - Rental data operations</li>
 * </ul>
 * 
 * <p><strong>JDBC Implementations:</strong></p>
 * <ul>
 * <li>{@link com.codeup.novabook.repo.impl.UserRepositoryImpl} - PostgreSQL user operations</li>
 * <li>{@link com.codeup.novabook.repo.impl.VehicleRepositoryImpl} - PostgreSQL vehicle operations</li>
 * <li>{@link com.codeup.novabook.repo.impl.RentalRepositoryImpl} - PostgreSQL rental operations</li>
 * </ul>
 * 
 * <p>
 * All repository implementations use {@link com.codeup.novabook.jdbc.JdbcTemplateLight}
 * for database operations and follow transactional patterns for data consistency.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
package com.codeup.novabook.repo;