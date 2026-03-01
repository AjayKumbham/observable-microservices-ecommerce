package com.ecommerce.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private CategoryDto category;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
