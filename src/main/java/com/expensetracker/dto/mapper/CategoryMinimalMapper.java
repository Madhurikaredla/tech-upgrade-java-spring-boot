package com.expensetracker.dto.mapper;

import com.expensetracker.dto.response.CategoryMinimalResponse;
import com.expensetracker.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMinimalMapper {
    CategoryMinimalResponse toMinimalResponse(Category category);
}
