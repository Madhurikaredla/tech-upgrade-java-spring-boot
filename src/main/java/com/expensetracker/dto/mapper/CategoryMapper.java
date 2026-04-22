package com.expensetracker.dto.mapper;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import org.mapstruct.*;

/**
 * CategoryMapper — MapStruct mapper for Category entity ↔ DTO conversions.
 *
 * Handles:
 *   - CategoryRequest → Category (for create/update operations)
 *   - Category → CategoryResponse (for read operations)
 *
 * Rules:
 *   - userId is NEVER mapped from a request DTO — it is set by the service layer
 *   - isSystemCategory is computed: true if userId is null, false otherwise
 *   - Audit fields (createdAt, updatedAt) are mapped to the response DTO
 *   - isDeleted is NEVER included in any response DTO
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Maps CategoryRequest to Category entity for create operations.
     * userId and audit fields are NOT set here — they are handled by the service layer.
     *
     * @param request the incoming request DTO
     * @return a new Category entity with name and categoryKey fields populated
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Category toEntity(CategoryRequest request);

    /**
     * Maps Category entity to CategoryResponse DTO for read operations.
     * isSystemCategory is computed: true if userId is null, false otherwise.
     *
     * @param category the Category entity
     * @return CategoryResponse DTO
     */
    @Mapping(target = "isSystemCategory", expression = "java(category.getUserId() == null)")
    CategoryResponse toResponse(Category category);

    // No updateEntityFromRequest for update; only name is updated directly in service
}
