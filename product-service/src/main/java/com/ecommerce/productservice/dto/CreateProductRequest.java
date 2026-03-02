package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\d\\s\\-'.&()]+$", message = "Product name contains invalid characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),!?;:/\\n\\r]*$", message = "Description contains invalid characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String imageUrl;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
