/**
 * Service layer containing business logic and application services.
 * <p>
 * This package contains the core business logic of the NovaBook system, implementing
 * use cases and orchestrating operations between the domain and repository layers.
 * </p>
 * <ul>
 * <li><strong>{@link com.codeup.novabook.service.impl.UserServiceImpl}:</strong> Manages user operations including
 *     authentication, registration, and profile management</li>
 * <li><strong>{@link com.codeup.novabook.service.impl.BookServiceImpl}:</strong> Handles book management
 *     including registration, updates, and availability tracking</li>
 * <li><strong>{@link com.codeup.novabook.service.impl.MemberServiceImpl}:</strong> Manages member operations
 *     including registration, updates, and status management</li>
 * <li><strong>{@link com.codeup.novabook.service.impl.LoanServiceImpl}:</strong> Orchestrates loan processes
 *     including creation, returns, extensions, and overdue management</li>
 * </ul>
 * 
 * <p>
 * All services follow the Single Responsibility Principle and provide transactional
 * boundaries for business operations. They validate input data and enforce business rules
 * before delegating to repository implementations.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago/Antonio Santiago
 * @version 1.0
 * @since 1.0
 */
package com.codeup.novabook.service;