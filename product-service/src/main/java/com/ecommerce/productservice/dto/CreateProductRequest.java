package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
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
