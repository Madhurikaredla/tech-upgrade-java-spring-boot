package com.expensetracker.dto.mapper;

import com.expensetracker.dto.response.CategoryMinimalResponse;
import com.expensetracker.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-22T18:52:08+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Homebrew)"
)
@Component
public class CategoryMinimalMapperImpl implements CategoryMinimalMapper {

    @Override
    public CategoryMinimalResponse toMinimalResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryMinimalResponse.CategoryMinimalResponseBuilder categoryMinimalResponse = CategoryMinimalResponse.builder();

        categoryMinimalResponse.id( category.getId() );
        categoryMinimalResponse.name( category.getName() );
        categoryMinimalResponse.categoryKey( category.getCategoryKey() );

        return categoryMinimalResponse.build();
    }
}
