package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Pattern(regexp = "^(?=.*\\p{L})[\\p{L}\\d\\s\\-'.&()]+$", message = "Product name contains invalid characters")
    @Schema(example = "Apple MacBook Pro 16-inch")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),!?;:/\\n\\r]*$", message = "Description contains invalid characters")
    @Schema(example = "M3 Max chip with 16-core CPU, 40-core GPU, 48GB Unified Memory, 1TB SSD Storage")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(example = "MACBK-PRO-16-M3M")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    @Schema(example = "3299.99")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(example = "45")
    private Integer stockQuantity;

    @Schema(example = "https://example.com/images/macbook-pro.jpg")
    private String imageUrl;

    @NotNull(message = "Category ID is required")
    @Schema(example = "1")
    private Long categoryId;
}
