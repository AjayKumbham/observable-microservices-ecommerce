package com.ecommerce.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
