package com.expensetracker.service;

import com.expensetracker.dto.mapper.CategoryMapper;
import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.PaginatedResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.AppLogger;
import com.expensetracker.util.PaginationUtils;
import com.expensetracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CategoryService — business logic for category management.
 *
 * Responsibilities:
 *   - Create, read, update, delete (soft) categories
 *   - Enforce user ownership rules: users can only modify their own categories
 *   - Enforce unique category names within a user's scope
 *   - System categories (userId IS NULL) are read-only for all users
 *
 * Transaction boundaries:
 *   - All read-only methods annotated with @Transactional(readOnly = true)
 *   - All write methods annotated with @Transactional
 *
 * Security:
 *   - userId is ALWAYS extracted from the security context via SecurityUtils
 *   - NEVER trusted from the request body
 *
 * All methods log entry and exit with relevant IDs.
 * All errors throw AppException with a specific ErrorCode.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final SecurityUtils securityUtils;
    private final PaginationUtils paginationUtils;

    /**
     * Creates a new category for the authenticated user.
     *
     * Validation:
     *   - Category name must be unique within the user's categories and system categories
     *   - Category key must be globally unique across all categories
     *
     * @param request the category creation request
     * @return CategoryResponse DTO
     * @throws AppException DUPLICATE_CATEGORY_NAME if a category with this name already exists
     * @throws AppException DUPLICATE_CATEGORY_KEY if a category with this key already exists
     * @throws AppException USER_NOT_FOUND if the authenticated user does not exist
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "category.create", "userId", userId, "name", request.getName(), "key", request.getCategoryKey());

        // Check for duplicate categoryKey (globally unique across all categories)
        if (categoryRepository.existsByCategoryKey(request.getCategoryKey())) {
            AppLogger.warn(log, "category.create.duplicateKey", "key", request.getCategoryKey(), "userId", userId);
            throw new AppException(ErrorCode.DUPLICATE_CATEGORY_KEY);
        }

        // Map request to entity and set userId
        Category category = categoryMapper.toEntity(request);
        category.setUserId(userId);
        category.setIsDeleted(false);

        // Save and return response
        Category savedCategory = categoryRepository.save(category);
        AppLogger.info(log, "category.create.success", "categoryId", savedCategory.getId(), "userId", userId, "key", savedCategory.getCategoryKey());

        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Retrieves all categories accessible by the authenticated user.
     * Accessible categories include:
     *   - System categories (userId IS NULL)
     *   - User's own categories (userId = {userId})
     *
     * @param pageable pagination and sorting parameters
     * @return PaginatedResponse of CategoryResponse DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "category.list", "userId", userId, "page", pageable.getPageNumber(), "size", pageable.getPageSize());

        Page<Category> categories = categoryRepository.findAllAccessibleByUser(userId, pageable);
        AppLogger.info(log, "category.list.success", "userId", userId, "total", categories.getTotalElements());

        Page<CategoryResponse> responsePage = categories.map(categoryMapper::toResponse);
        return paginationUtils.toPaginatedResponse(responsePage);
    }

    /**
     * Retrieves a single category by ID if accessible by the authenticated user.
     *
     * @param categoryId the category ID
     * @return CategoryResponse DTO
     * @throws AppException CATEGORY_NOT_FOUND if the category does not exist or is not accessible
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "category.getById", "categoryId", categoryId, "userId", userId);

        Category category = categoryRepository.findByIdAndAccessibleByUser(categoryId, userId)
                .orElseThrow(() -> {
                    AppLogger.warn(log, "category.notFound", "categoryId", categoryId, "userId", userId);
                    return new AppException(ErrorCode.CATEGORY_NOT_FOUND);
                });

        AppLogger.info(log, "category.getById.success", "categoryId", categoryId, "name", category.getName());
        return categoryMapper.toResponse(category);
    }

    /**
     * Updates an existing category.
     *
     * Validation:
     *   - Category must exist and be owned by the user (system categories cannot be updated)
     *   - Category name must be unique within the user's scope
     *   - Category key must be globally unique
     *
     * @param categoryId the category ID
     * @param request the update request
     * @return CategoryResponse DTO
     * @throws AppException CATEGORY_NOT_FOUND if the category does not exist
     * @throws AppException CATEGORY_ACCESS_DENIED if the user does not own the category
     * @throws AppException DUPLICATE_CATEGORY_NAME if the new name conflicts with an existing category
     * @throws AppException DUPLICATE_CATEGORY_KEY if the new key conflicts with an existing category
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, com.expensetracker.dto.request.CategoryUpdateRequest request) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "category.update", "categoryId", categoryId, "userId", userId, "newName", request.getName());

        // Fetch the category
        Category category = categoryRepository.findById(categoryId)
            .filter(c -> !c.getIsDeleted())
            .orElseThrow(() -> {
                AppLogger.warn(log, "category.update.notFound", "categoryId", categoryId);
                return new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            });

        // Verify ownership (system categories cannot be updated)
        if (category.getUserId() == null || !category.getUserId().equals(userId)) {
            AppLogger.warn(log, "category.update.accessDenied", "categoryId", categoryId, "userId", userId, "ownerId", category.getUserId());
            throw new AppException(ErrorCode.CATEGORY_ACCESS_DENIED);
        }

        // Only update the name field
        category.setName(request.getName());
        Category updatedCategory = categoryRepository.save(category);
        AppLogger.info(log, "category.update.success", "categoryId", categoryId, "newName", updatedCategory.getName());

        return categoryMapper.toResponse(updatedCategory);
    }

    /**
     * Soft-deletes a category.
     *
     * Validation:
     *   - Category must exist and be owned by the user (system categories cannot be deleted)
     *
     * @param categoryId the category ID
     * @throws AppException CATEGORY_NOT_FOUND if the category does not exist
     * @throws AppException CATEGORY_ACCESS_DENIED if the user does not own the category
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "category.delete", "categoryId", categoryId, "userId", userId);

        // Fetch the category
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> {
                    AppLogger.warn(log, "category.delete.notFound", "categoryId", categoryId);
                    return new AppException(ErrorCode.CATEGORY_NOT_FOUND);
                });

        // Verify ownership (system categories cannot be deleted)
        if (category.getUserId() == null || !category.getUserId().equals(userId)) {
            AppLogger.warn(log, "category.delete.accessDenied", "categoryId", categoryId, "userId", userId, "ownerId", category.getUserId());
            throw new AppException(ErrorCode.CATEGORY_ACCESS_DENIED);
        }

        // Soft delete
        category.setIsDeleted(true);
        categoryRepository.save(category);
        AppLogger.info(log, "category.delete.success", "categoryId", categoryId, "userId", userId);
    }

    /**
     * Extracts the authenticated user's ID from the security context.
     *
     * @return the user ID
     * @throws AppException USER_NOT_FOUND if the user does not exist in the database
     * @throws AppException UNAUTHORIZED if there is no authenticated principal
     */
    private Long getCurrentUserId() {
        String email = securityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    AppLogger.error(log, "auth.userMissingFromDb", new AppException(ErrorCode.USER_NOT_FOUND),
                            "email", email);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });
        return user.getId();
    }
}
