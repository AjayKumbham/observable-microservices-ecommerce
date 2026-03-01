package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryUpdateRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    /** ABSOLUTE sets exact stock; DELTA adds/subtracts */
    public enum UpdateMode { ABSOLUTE, DELTA }

    @NotNull(message = "Update mode is required")
    private UpdateMode mode;
}
