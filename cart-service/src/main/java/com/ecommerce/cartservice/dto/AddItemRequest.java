package com.ecommerce.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class AddItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(example = "1")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(example = "2")
    private Integer quantity;
}
