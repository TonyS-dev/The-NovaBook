/**
 * Domain layer containing core business entities for the LibroNova library management system.
 * <p>
 * This package contains the fundamental building blocks of the LibroNova application,
 * representing the core business domain in a framework-agnostic way.
 * </p>
 * 
 * <h2>Core Entities:</h2>
 * <ul>
 *   <li><strong>{@link com.codeup.novabook.domain.User}</strong> - System users (ADMIN/ASSISTANT roles)</li>
 *   <li><strong>{@link com.codeup.novabook.domain.Member}</strong> - Library members who can request loans</li>
 *   <li><strong>{@link com.codeup.novabook.domain.Book}</strong> - Book catalog with inventory management</li>
 *   <li><strong>{@link com.codeup.novabook.domain.Loan}</strong> - Loan transactions with fine calculation</li>
 * </ul>
 * 
 * <h2>Enumerations:</h2>
 * <ul>
 *   <li><strong>{@link com.codeup.novabook.domain.UserRole}</strong> - User roles (ADMIN, ASSISTANT)</li>
 *   <li><strong>{@link com.codeup.novabook.domain.UserStatus}</strong> - User account status (ACTIVE, INACTIVE)</li>
 *   <li><strong>{@link com.codeup.novabook.domain.MemberStatus}</strong> - Member status (ACTIVE, INACTIVE, SUSPENDED)</li>
 *   <li><strong>{@link com.codeup.novabook.domain.LoanStatus}</strong> - Loan status (ACTIVE, RETURNED, OVERDUE)</li>
 * </ul>
 * 
 * <h2>Design Principles:</h2>
 * <ul>
 *   <li>All entities use Integer IDs (SERIAL in PostgreSQL) for simplicity and performance</li>
 *   <li>Entities contain business logic methods (e.g., fine calculation, stock validation)</li>
 *   <li>No external framework dependencies - pure Java domain model</li>
 *   <li>LocalDateTime/LocalDate for temporal fields (JDBC-friendly)</li>
 *   <li>BigDecimal for monetary values (precise calculations)</li>
 * </ul>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Creating a new book
 * Book book = new Book("978-3-16-148410-0", "1984", "George Orwell", "Science Fiction");
 * book.setTotalCopies(5);
 * book.setAvailableCopies(5);
 * 
 * // Creating a loan
 * Loan loan = new Loan(memberId, bookId, 7);
 * loan.setStatus(LoanStatus.ACTIVE);
 * 
 * // Returning a book with fine calculation
 * loan.markAsReturned(new BigDecimal("1500"));
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
package com.codeup.novabook.domain;
