package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String imageUrl;

    private Long categoryId;

    private Boolean active;
}
