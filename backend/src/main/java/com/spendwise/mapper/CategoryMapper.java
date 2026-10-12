package com.spendwise.mapper;

import com.spendwise.dto.CategoryRequest;
import com.spendwise.dto.CategoryResponse;
import com.spendwise.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setType(request.getType());
        category.setDescription(request.getDescription());
        return category;
    }

    public void updateEntity(CategoryRequest request, Category category) {
        category.setCategoryName(request.getCategoryName());
        category.setType(request.getType());
        category.setDescription(request.getDescription());
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setType(category.getType());
        response.setDescription(category.getDescription());
        response.setOwnerId(category.getOwner() != null ? category.getOwner().getUserId() : null);
        response.setIsPredefined(category.getOwner() == null);
        return response;
    }
}
