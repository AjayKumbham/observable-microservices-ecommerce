package com.ecommerce.notificationservice.dto;

import com.ecommerce.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Recipient is required")
    @Size(max = 255, message = "Recipient cannot exceed 255 characters")
    @Pattern(regexp = "^[A-Za-z0-9@.+_\\-\\s]+$", message = "Recipient contains invalid characters")
    private String recipient;

    @NotBlank(message = "Subject is required")
    @Size(min = 3, max = 255, message = "Subject must be between 3 and 255 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&()!?;:]+$", message = "Subject contains invalid characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(min = 1, max = 5000, message = "Body cannot exceed 5000 characters")
    @Pattern(regexp = "^[\\p{L}\\d\\s\\-'.&(),!?;:/\\n\\r]*$", message = "Body contains invalid characters")
    private String body;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private Long referenceId;    // orderId / userId for traceability
}
