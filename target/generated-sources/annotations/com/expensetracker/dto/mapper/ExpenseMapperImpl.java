package com.expensetracker.dto.mapper;

import com.expensetracker.dto.request.ExpenseRequest;
import com.expensetracker.dto.response.ExpenseResponse;
import com.expensetracker.entity.Expense;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-22T18:52:08+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Homebrew)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public Expense toEntity(ExpenseRequest request) {
        if ( request == null ) {
            return null;
        }

        Expense.ExpenseBuilder expense = Expense.builder();

        expense.amount( request.getAmount() );
        expense.expenseDate( request.getExpenseDate() );
        expense.description( request.getDescription() );

        return expense.build();
    }

    @Override
    public ExpenseResponse toResponse(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseResponse.ExpenseResponseBuilder expenseResponse = ExpenseResponse.builder();

        expenseResponse.id( expense.getId() );
        expenseResponse.amount( expense.getAmount() );
        expenseResponse.description( expense.getDescription() );
        expenseResponse.expenseDate( expense.getExpenseDate() );
        expenseResponse.userId( expense.getUserId() );
        expenseResponse.createdAt( expense.getCreatedAt() );
        expenseResponse.updatedAt( expense.getUpdatedAt() );

        return expenseResponse.build();
    }
}
