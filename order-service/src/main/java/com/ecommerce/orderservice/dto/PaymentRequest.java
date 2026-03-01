package com.ecommerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Client DTO for calling payment-service /api/v1/payments/charge */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
}
