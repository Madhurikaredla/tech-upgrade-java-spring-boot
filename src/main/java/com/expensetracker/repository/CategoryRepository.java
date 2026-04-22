package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CategoryRepository — data access layer for Category entity.
 *
 * Rules enforced:
 *   - All queries filter WHERE is_deleted = false
 *   - All user-scoped queries filter by userId (or userId IS NULL for system categories)
 *   - All list queries use Pageable — no unbounded findAll()
 *   - JPQL is used for custom queries where method names are insufficient
 *
 * Users can access:
 *   - System categories (userId IS NULL)
 *   - Their own categories (userId = {userId})
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by ID, only if not soft-deleted and accessible by the user.
     * Accessible means: either system category (user_id IS NULL) or owned by userId.
     *
     * @param id category ID
     * @param userId the authenticated user's ID
     * @return Optional containing the category if found and accessible
     */
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isDeleted = false " +
            "AND (c.userId IS NULL OR c.userId = :userId)")
    Optional<Category> findByIdAndAccessibleByUser(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Finds all categories accessible by the user (system categories + user's own categories).
     * Filters out soft-deleted records.
     * Returns a paginated result.
     *
     * @param userId the authenticated user's ID
     * @param pageable pagination and sorting parameters
     * @return Page of categories
     */
    @Query("SELECT c FROM Category c WHERE c.isDeleted = false " +
            "AND (c.userId IS NULL OR c.userId = :userId)")
    Page<Category> findAllAccessibleByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Checks if a category with the given name already exists for the user.
     * This is used to enforce unique category names within a user's scope.
     * System categories are also checked to prevent conflicts.
     *
     * @param name the category name
     * @param userId the authenticated user's ID
     * @return true if a category with this name exists (either system or user-owned)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name " +
            "AND c.isDeleted = false " +
            "AND (c.userId IS NULL OR c.userId = :userId)")
    boolean existsByNameAndAccessibleByUser(@Param("name") String name, @Param("userId") Long userId);

    /**
     * Checks if a category with the given name already exists for the user,
     * excluding the category with the given ID.
     * Used during update operations to allow keeping the same name.
     *
     * @param name the category name
     * @param userId the authenticated user's ID
     * @param categoryId the ID of the category being updated (to exclude from the check)
     * @return true if a conflicting category exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name " +
            "AND c.isDeleted = false " +
            "AND c.id != :categoryId " +
            "AND (c.userId IS NULL OR c.userId = :userId)")
    boolean existsByNameAndAccessibleByUserExcludingId(@Param("name") String name,
                                                         @Param("userId") Long userId,
                                                         @Param("categoryId") Long categoryId);

    /**
     * Checks if a category with the given categoryKey already exists.
     * categoryKey must be globally unique across all categories (system and user-defined).
     * This is enforced at the database level by uq_categories_key constraint,
     * but this method allows for explicit validation before save.
     *
     * @param categoryKey the category key
     * @return true if a category with this key already exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.categoryKey = :categoryKey " +
            "AND c.isDeleted = false")
    boolean existsByCategoryKey(@Param("categoryKey") String categoryKey);

    /**
     * Checks if a category with the given categoryKey already exists,
     * excluding the category with the given ID.
     * Used during update operations to allow keeping the same key.
     *
     * @param categoryKey the category key
     * @param categoryId the ID of the category being updated (to exclude from the check)
     * @return true if a conflicting category exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.categoryKey = :categoryKey " +
            "AND c.isDeleted = false " +
            "AND c.id != :categoryId")
    boolean existsByCategoryKeyExcludingId(@Param("categoryKey") String categoryKey,
                                            @Param("categoryId") Long categoryId);

    /**
     * Finds all categories from the given ID list that are accessible by the user
     * (system categories or user-owned) and not soft-deleted.
     * Used by ExpenseService to validate that all provided categoryIds are valid.
     *
     * @param categoryIds list of category IDs to validate
     * @param userId      the authenticated user's ID
     * @return list of matching accessible Category entities
     */
    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds " +
            "AND c.isDeleted = false " +
            "AND (c.userId IS NULL OR c.userId = :userId)")
    java.util.List<Category> findAllAccessibleByUserAndIds(
            @Param("categoryIds") java.util.List<Long> categoryIds,
            @Param("userId") Long userId);
}
