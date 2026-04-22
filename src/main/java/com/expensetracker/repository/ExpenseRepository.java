package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.isDeleted = false AND e.userId = :userId ORDER BY e.expenseDate DESC, e.id DESC")
    Page<Expense> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.isDeleted = false AND e.userId = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
