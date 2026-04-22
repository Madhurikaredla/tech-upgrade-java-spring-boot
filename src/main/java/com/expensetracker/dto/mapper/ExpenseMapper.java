package com.expensetracker.dto.mapper;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.dto.response.ExpenseMinimalResponse;
import com.expensetracker.dto.response.CategoryMinimalResponse;
import com.expensetracker.entity.Expense;import org.mapstruct.BeanMapping;import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * ExpenseMapper — MapStruct mapper for Expense entity ↔ DTO conversions.
 *
 * categories is NOT mapped here — it is resolved and set by the service layer
 * after loading CategoryResponse objects from the repository.
 */
@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    /**
     * Maps Expense entity to ExpenseMinimalResponse DTO.
     * categories field is intentionally left null here — the service sets it.
     */
    default ExpenseMinimalResponse toMinimalResponse(Expense expense, List<CategoryMinimalResponse> categories) {
        if (expense == null) return null;
        return ExpenseMinimalResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .categories(categories)
                .build();
    }

    /**
     * Maps ExpenseRequest to Expense entity for create/update operations.
     * userId and audit fields are set by the service layer.
     * categoryIds from the request are not a field on Expense — explicitly source-ignored to suppress warnings.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @BeanMapping(ignoreByDefault = false)
    Expense toEntity(ExpenseRequest request);

    /**
     * Maps Expense entity to ExpenseResponse DTO.
     * categories field is intentionally left null here — the service sets it.
     */
    @Mapping(target = "categories", ignore = true)
    ExpenseResponse toResponse(Expense expense);

    /**
     * Builds a full ExpenseResponse with categories populated.
     * Used by the service after resolving category details.
     *
     * @param expense    the expense entity
     * @param categories resolved CategoryResponse list
     * @return fully populated ExpenseResponse
     */
    default ExpenseResponse toResponseWithCategories(Expense expense, List<CategoryResponse> categories) {
        ExpenseResponse response = toResponse(expense);
        response.setCategories(categories);
        return response;
    }
}
