package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),#]+$", message = "Shipping address contains invalid characters")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),!?;:]*$", message = "Notes contain invalid characters")
    private String notes;
}
