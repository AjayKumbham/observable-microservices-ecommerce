package com.ecommerce.notificationservice.dto;

import com.ecommerce.notificationservice.model.NotificationStatus;
import com.ecommerce.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String recipient;
    private String subject;
    private NotificationType type;
    private NotificationStatus status;
    private Long referenceId;
    private LocalDateTime sentAt;
}
