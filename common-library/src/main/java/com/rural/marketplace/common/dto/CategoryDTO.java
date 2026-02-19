package com.rural.marketplace.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Data Transfer Object for Category information.
 * Implements {@link Serializable} to support caching in Redis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private Long parentId;
}
