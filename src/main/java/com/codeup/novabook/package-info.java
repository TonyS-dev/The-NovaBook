/**
 * NovaBook - Library Management System.
 * <p>
 * This package contains the core application architecture for the NovaBook library management system.
 * The system follows clean architecture principles with clear separation of concerns across layers:
 * </p>
 * <ul>
 * <li><strong>Domain Layer:</strong> Contains business entities and interfaces ({@link com.codeup.novabook.domain})</li>
 * <li><strong>Service Layer:</strong> Contains business logic and application services ({@link com.codeup.novabook.service})</li>
 * <li><strong>Repository Layer:</strong> Contains data access abstractions and implementations ({@link com.codeup.novabook.repo})</li>
 * <li><strong>UI Layer:</strong> Contains JavaFX user interface components ({@link com.codeup.novabook.ui})</li>
 * <li><strong>Infrastructure:</strong> Contains technical concerns like database connections and utilities</li>
 * </ul>
 * 
 * <p>
 * The application manages vehicle rentals for cars and bicycles, providing secure user authentication,
 * real-time availability tracking, and comprehensive reporting capabilities.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
package com.codeup.novabook;