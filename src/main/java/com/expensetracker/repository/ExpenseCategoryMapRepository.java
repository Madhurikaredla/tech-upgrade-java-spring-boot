package com.expensetracker.repository;

import com.expensetracker.entity.ExpenseCategoryMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ExpenseCategoryMapRepository — data access for the expense-to-category mapping table.
 *
 * All deletes are soft deletes — isDeleted is set to true, no SQL DELETE is issued.
 * All reads filter WHERE is_deleted = false.
 */
@Repository
public interface ExpenseCategoryMapRepository extends JpaRepository<ExpenseCategoryMap, Long> {

    /**
     * Returns all active (non-deleted) category mappings for the given expense.
     */
    List<ExpenseCategoryMap> findByExpenseIdAndIsDeletedFalse(Long expenseId);

    /**
     * Soft-deletes all category mappings for the given expense.
     * Used when deleting or updating an expense's categories.
     */
    @Transactional
    @Modifying
    @Query("UPDATE ExpenseCategoryMap m SET m.isDeleted = true WHERE m.expenseId = :expenseId AND m.isDeleted = false")
    void softDeleteByExpenseId(@Param("expenseId") Long expenseId);
}
