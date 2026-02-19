package com.rural.marketplace.catalog.service;

import com.rural.marketplace.common.dto.CategoryDTO;
import com.rural.marketplace.catalog.entity.Category;
import com.rural.marketplace.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rural.marketplace.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing product categories.
 * Implements caching for broad category lookups to optimize user experience.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Creates a new category and evicts the 'categories' list cache.
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setParentId(categoryDTO.getParentId());

        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    /**
     * Retrieves all categories.
     * Results are cached for high-speed access to the category tree.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific category by ID.
     * Uses the 'category' cache with ID as key.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "category", key = "#id")
    public CategoryDTO getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
        return mapToDTO(category);
    }

    /**
     * Updates an existing category.
     * Evicts both the specific category cache and the general category list.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "categories", allEntries = true)
    })
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setParentId(categoryDTO.getParentId());

        Category updatedCategory = categoryRepository.save(category);
        return mapToDTO(updatedCategory);
    }

    /**
     * Deletes a category and invalidates associated caches.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "categories", allEntries = true)
    })
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParentId());
        return dto;
    }
}
