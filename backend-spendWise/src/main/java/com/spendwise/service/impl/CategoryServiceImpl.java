package com.spendwise.service.impl;

import com.spendwise.dto.CategoryRequest;
import com.spendwise.dto.CategoryResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.exception.DuplicateResourceException;
import com.spendwise.exception.ForbiddenException;
import com.spendwise.exception.InvalidOperationException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.mapper.CategoryMapper;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.CategoryService;
import com.spendwise.service.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               TransactionRepository transactionRepository,
                               CategoryMapper mapper,
                               CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAll(Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();
        return categoryRepository.findByOwnerIsNullOrOwnerUserId(currentUser.getUserId(), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (category.getOwner() != null) {
            User currentUser = currentUserProvider.getCurrentUser();
            if (!category.getOwner().getUserId().equals(currentUser.getUserId())) {
                throw new ForbiddenException("Cannot access this category");
            }
        }
        return mapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (categoryRepository.findByCategoryNameAndOwnerUserIdAndType(request.getCategoryName(), currentUser.getUserId(), request.getType()).isPresent()) {
            throw new DuplicateResourceException("Category already exists");
        }
        Category category = mapper.toEntity(request);
        category.setOwner(currentUser);
        return mapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (category.getOwner() == null) {
            throw new InvalidOperationException("Predefined categories cannot be modified");
        }
        User currentUser = currentUserProvider.getCurrentUser();
        if (!category.getOwner().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot modify this category");
        }
        categoryRepository.findByCategoryNameAndOwnerUserIdAndType(request.getCategoryName(), currentUser.getUserId(), request.getType())
                .ifPresent(existing -> {
                    if (!existing.getCategoryId().equals(id)) {
                        throw new DuplicateResourceException("Category already exists");
                    }
                });
        mapper.updateEntity(request, category);
        return mapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (category.getOwner() == null) {
            throw new InvalidOperationException("Predefined categories cannot be deleted");
        }
        User currentUser = currentUserProvider.getCurrentUser();
        if (!category.getOwner().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Cannot delete this category");
        }
        if (transactionRepository.findByUserUserIdAndCategoryCategoryId(currentUser.getUserId(), id).stream().findAny().isPresent()) {
            throw new InvalidOperationException("Category is referenced by transactions");
        }
        categoryRepository.delete(category);
    }
}
