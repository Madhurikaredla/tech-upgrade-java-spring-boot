package com.expensetracker.service;

import com.expensetracker.dto.mapper.CategoryMapper;
import com.expensetracker.dto.mapper.ExpenseMapper;
import com.expensetracker.dto.mapper.CategoryMinimalMapper;
import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.PaginatedResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.ExpenseCategoryMap;
import com.expensetracker.exception.AppException;
import com.expensetracker.exception.ErrorCode;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseCategoryMapRepository;
import com.expensetracker.repository.ExpenseRepository;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * ExpenseService — business logic for expense management.
 *
 * Responsibilities:
 *   - Create, read, update, soft-delete expenses
 *   - Enforce user ownership: users can only access/modify their own expenses
 *   - Manage expense-to-category mappings via ExpenseCategoryMap
 *   - Validate that all provided categoryIds exist and are accessible by the user
 *
 * Transaction boundaries:
 *   - All read-only methods: @Transactional(readOnly = true)
 *   - All write methods: @Transactional
 *
 * Security:
 *   - userId always extracted from security context — never from request body
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryMapRepository expenseCategoryMapRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;
    private final CategoryMapper categoryMapper;
    private final PaginationUtils paginationUtils;
    private final SecurityUtils securityUtils;
    private final CategoryMinimalMapper categoryMinimalMapper;

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Creates a new expense with the given categories for the authenticated user.
     *
     * @param request the expense creation request (amount, description, date, categoryIds)
     * @return ExpenseResponse with full category details
     */
    @Transactional
    public com.expensetracker.dto.response.ExpenseMinimalResponse createExpense(ExpenseRequest request) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "expense.create", "userId", userId, "amount", request.getAmount());

        // Validate and resolve categories — all must exist and be accessible by user
        List<Category> categories = resolveAndValidateCategories(request.getCategoryIds(), userId);

        // Persist the expense
        Expense expense = expenseMapper.toEntity(request);
        expense.setUserId(userId);
        expense.setIsDeleted(false);
        Expense saved = expenseRepository.save(expense);

        // Persist expense-category mappings
        saveExpenseCategoryMappings(saved.getId(), request.getCategoryIds());

        AppLogger.info(log, "expense.create.success", "expenseId", saved.getId(), "userId", userId, "categories", request.getCategoryIds());
        // Map to minimal response
        List<com.expensetracker.dto.response.CategoryMinimalResponse> minimalCategories = categories.stream()
            .map(categoryMinimalMapper::toMinimalResponse)
            .collect(Collectors.toList());
        return expenseMapper.toMinimalResponse(saved, minimalCategories);
    }

    // ── Get All (paginated) ───────────────────────────────────────────────────

    /**
     * Retrieves all expenses for the authenticated user, paginated.
     *
     * @param pageable pagination and sort parameters
     * @return PaginatedResponse of ExpenseResponse DTOs
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<com.expensetracker.dto.response.ExpenseMinimalResponse> getAllExpenses(Pageable pageable) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "expense.list", "userId", userId, "page", pageable.getPageNumber(), "size", pageable.getPageSize());

        Page<Expense> expensePage = expenseRepository.findAllByUserId(userId, pageable);
        AppLogger.info(log, "expense.list.success", "userId", userId, "total", expensePage.getTotalElements());

        Page<com.expensetracker.dto.response.ExpenseMinimalResponse> responsePage = expensePage.map(expense -> {
            List<Category> categories = resolveCategories(expense.getId());
            List<com.expensetracker.dto.response.CategoryMinimalResponse> minimalCategories = categories.stream()
                .map(categoryMinimalMapper::toMinimalResponse)
                .collect(Collectors.toList());
            return expenseMapper.toMinimalResponse(expense, minimalCategories);
        });

        return paginationUtils.toPaginatedResponse(responsePage);
    }

    // ── Get By ID ─────────────────────────────────────────────────────────────

    /**
     * Retrieves a single expense by ID, verifying the user owns it.
     *
     * @param expenseId the expense ID
     * @return ExpenseResponse with category details
     * @throws AppException EXPENSE_NOT_FOUND if not found or not owned by user
     */
    @Transactional(readOnly = true)
    public com.expensetracker.dto.response.ExpenseMinimalResponse getExpenseById(Long expenseId) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "expense.getById", "expenseId", expenseId, "userId", userId);

        Expense expense = fetchExpenseForUser(expenseId, userId);
        List<Category> categories = resolveCategories(expenseId);
        List<com.expensetracker.dto.response.CategoryMinimalResponse> minimalCategories = categories.stream()
            .map(categoryMinimalMapper::toMinimalResponse)
            .collect(Collectors.toList());

        AppLogger.info(log, "expense.getById.success", "expenseId", expenseId, "userId", userId);
        return expenseMapper.toMinimalResponse(expense, minimalCategories);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing expense (amount, description, date, categories).
     * Old category mappings are soft-deleted and replaced with the new set.
     *
     * @param expenseId the expense ID
     * @param request   the update request
     * @return updated ExpenseResponse
     * @throws AppException EXPENSE_NOT_FOUND if not found or not owned by user
     */
    @Transactional
    public com.expensetracker.dto.response.ExpenseMinimalResponse updateExpense(Long expenseId, ExpenseRequest request) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "expense.update", "expenseId", expenseId, "userId", userId, "amount", request.getAmount());

        Expense expense = fetchExpenseForUser(expenseId, userId);

        // Validate new categories
        List<Category> categories = resolveAndValidateCategories(request.getCategoryIds(), userId);

        // Update expense fields
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        Expense updated = expenseRepository.save(expense);

        // Soft-delete old category mappings
        expenseCategoryMapRepository.softDeleteByExpenseId(expenseId);
        // Only insert new mappings for unique categoryIds (avoid duplicates)
        List<Long> uniqueCategoryIds = request.getCategoryIds().stream().distinct().collect(Collectors.toList());
        saveExpenseCategoryMappings(expenseId, uniqueCategoryIds);

        AppLogger.info(log, "expense.update.success", "expenseId", expenseId, "userId", userId, "categories", uniqueCategoryIds);
        // Map to minimal response
        List<com.expensetracker.dto.response.CategoryMinimalResponse> minimalCategories = categories.stream()
            .map(categoryMinimalMapper::toMinimalResponse)
            .collect(Collectors.toList());
        return expenseMapper.toMinimalResponse(updated, minimalCategories);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes an expense. Also soft-deletes all its category mappings.
     *
     * @param expenseId the expense ID
     * @throws AppException EXPENSE_NOT_FOUND if not found or not owned by user
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        Long userId = getCurrentUserId();
        AppLogger.info(log, "expense.delete", "expenseId", expenseId, "userId", userId);

        Expense expense = fetchExpenseForUser(expenseId, userId);

        // Soft-delete category mappings first
        expenseCategoryMapRepository.softDeleteByExpenseId(expenseId);

        // Soft-delete the expense
        expense.setIsDeleted(true);
        expenseRepository.save(expense);

        AppLogger.info(log, "expense.delete.success", "expenseId", expenseId, "userId", userId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Fetches an expense by ID, enforcing ownership by userId.
     * Throws EXPENSE_NOT_FOUND if not found, deleted, or not owned by the user.
     */
    private Expense fetchExpenseForUser(Long expenseId, Long userId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> {
                    AppLogger.warn(log, "expense.notFound", "expenseId", expenseId, "userId", userId);
                    return new AppException(ErrorCode.EXPENSE_NOT_FOUND);
                });
    }

    /**
     * Resolves category entities by IDs and verifies they are accessible by the user.
     * Throws EXPENSE_CATEGORY_REQUIRED if any category ID is invalid or inaccessible.
     */
    private List<Category> resolveAndValidateCategories(List<Long> categoryIds, Long userId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.EXPENSE_CATEGORY_REQUIRED);
        }
        List<Category> categories = categoryRepository.findAllAccessibleByUserAndIds(categoryIds, userId);
        if (categories.size() != categoryIds.size()) {
            // Determine which IDs were not found or not accessible
            List<Long> foundIds = categories.stream().map(Category::getId).collect(Collectors.toList());
            List<Long> invalidIds = categoryIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            AppLogger.warn(log, "expense.categoryNotFound", "invalidCategoryIds", invalidIds, "userId", userId);
            throw new AppException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    /**
     * Loads the Category entities for a given expense via its active mappings.
     */
    private List<Category> resolveCategories(Long expenseId) {
        List<Long> categoryIds = expenseCategoryMapRepository.findByExpenseIdAndIsDeletedFalse(expenseId)
            .stream()
            .map(ExpenseCategoryMap::getCategoryId)
            .collect(Collectors.toList());
        List<Category> categories = categoryRepository.findAllById((Iterable<Long>) categoryIds);
        return categories;
    }

    /**
     * Persists ExpenseCategoryMap entries for the given expense and category IDs.
     *
     * Safe to call after softDeleteByExpenseId because the unique index on
     * (expense_id, category_id) is a partial index (WHERE is_deleted = false).
     * Soft-deleted rows are excluded from the index, so new rows never conflict.
     */
    private void saveExpenseCategoryMappings(Long expenseId, List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            ExpenseCategoryMap map = ExpenseCategoryMap.builder()
                .expenseId(expenseId)
                .categoryId(categoryId)
                .isDeleted(false)
                .build();
            expenseCategoryMapRepository.save(map);
        }
    }

    /**
     * Builds an ExpenseResponse from the expense entity and resolved categories.
     */
    private ExpenseResponse buildExpenseResponse(Expense expense, List<Category> categories) {
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        return expenseMapper.toResponseWithCategories(expense, categoryResponses);
    }

    /**
     * Extracts the authenticated user's ID from the security context via UserRepository.
     */
    private Long getCurrentUserId() {
        String email = securityUtils.getCurrentUserEmail();
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }
}
