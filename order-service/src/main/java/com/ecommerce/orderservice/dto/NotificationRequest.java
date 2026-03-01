package com.ecommerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Client DTO for calling notification-service /api/v1/notifications/send */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationRequest {
    private String recipient;
    private String subject;
    private String body;
    private String type;         // EMAIL | SMS
    private Long referenceId;
}
