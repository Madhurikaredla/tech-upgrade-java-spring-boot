package com.expensetracker.validation;

import com.expensetracker.enums.SortDirection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SortDirectionValidator implements ConstraintValidator<ValidSortDirection, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return SortDirection.isValid(value);
    }
}
