/**
 * Service layer containing business logic and application services.
 * <p>
 * This package contains the core business logic of the EcoFleet system, implementing
 * use cases and orchestrating operations between the domain and repository layers.
 * </p>
 * <ul>
 * <li><strong>{@link com.codeup.novabook.service.impl.UserService}:</strong> Manages user operations including
 *     authentication, registration, and profile management</li>
 * <li><strong>{@link com.codeup.novabook.service.impl.VehicleService}:</strong> Handles vehicle management
 *     including registration, updates, and availability tracking</li>
 * <li><strong>{@link com.codeup.novabook.service.impl.RentalService}:</strong> Orchestrates rental processes
 *     including creation, closure, and status management</li>
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