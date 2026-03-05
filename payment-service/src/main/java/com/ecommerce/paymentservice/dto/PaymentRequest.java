package com.ecommerce.paymentservice.dto;

import com.ecommerce.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    @Schema(example = "101")
    private Long orderId;

    @NotNull(message = "User ID is required")
    @Schema(example = "1")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Schema(example = "6599.98")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Schema(example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;
}
