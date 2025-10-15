package com.codeup.novabook.repo;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for managing entities.
 * 
 * <p>This interface defines the contract for basic CRUD (Create, Read, Update, Delete) operations
 * on entities of type {@code T} identified by {@code ID}. Implementations should handle data access
 * and ensure proper error handling and validation.</p>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>Entity Creation</strong> - Persist new entities</li>
 *   <li><strong>Entity Retrieval</strong> - Find entities by ID or retrieve all</li>
 *   <li><strong>Entity Updates</strong> - Modify existing entities</li>
 *   <li><strong>Entity Deletion</strong> - Remove entities by ID</li>
 * </ul>
 * 
 * <p><strong>Implementation Notes:</strong></p>
 * <ul>
 *   <li>Methods may throw exceptions for invalid operations (e.g., not found)</li>
 *   <li>Transactional behavior should be considered in implementations</li>
 *   <li>Thread-safety depends on the underlying data store</li>
 * </ul>
 * 
 * @param <T> the entity type
 * @param <ID> the identifier type
 * @version 1.0
 * @since 1.0
 */
public interface IGeneralRepository<T, ID> {
    T create(T t);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T t);
    void delete(ID id);
}