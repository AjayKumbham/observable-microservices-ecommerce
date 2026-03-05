package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Schema(example = "Electronics")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(example = "Electronic devices, computers, and accessories.")
    private String description;

    @Schema(example = "https://example.com/images/electronics-cat.png")
    private String imageUrl;
}
