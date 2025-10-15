/**
 * Repository layer providing data access abstractions and implementations.
 * <p>
 * This package contains the data access layer of the NovaBook system, following
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
 * <li>{@link com.codeup.novabook.repo.IBookRepository} - Book data operations</li>
 * <li>{@link com.codeup.novabook.repo.IMemberRepository} - Member data operations</li>
 * <li>{@link com.codeup.novabook.repo.ILoanRepository} - Loan data operations</li>
 * </ul>
 * 
 * <p><strong>JDBC Implementations:</strong></p>
 * <ul>
 * <li>{@link com.codeup.novabook.repo.impl.UserRepositoryImpl} - PostgreSQL user operations</li>
 * <li>{@link com.codeup.novabook.repo.impl.BookRepositoryImpl} - PostgreSQL book operations</li>
 * <li>{@link com.codeup.novabook.repo.impl.MemberRepositoryImpl} - PostgreSQL member operations</li>
 * <li>{@link com.codeup.novabook.repo.impl.LoanRepositoryImpl} - PostgreSQL loan operations</li>
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