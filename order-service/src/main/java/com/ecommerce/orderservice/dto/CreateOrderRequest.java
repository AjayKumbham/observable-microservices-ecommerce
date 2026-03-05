package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "1")
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),#]+$", message = "Shipping address contains invalid characters")
    @Schema(example = "123 Main Street, Tech Park, Hyderabad, Telangana, 500081")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    @Schema(example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Schema(description = "List of items to order")
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),!?;:]*$", message = "Notes contain invalid characters")
    @Schema(example = "Please leave the package at the front door.")
    private String notes;
}
