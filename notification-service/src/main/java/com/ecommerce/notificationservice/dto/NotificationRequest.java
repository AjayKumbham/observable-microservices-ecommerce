package com.ecommerce.notificationservice.dto;

import com.ecommerce.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private Long referenceId;    // orderId / userId for traceability
}
