package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.NotificationRequest;
import com.ecommerce.notificationservice.dto.NotificationResponse;
import com.ecommerce.notificationservice.model.Notification;
import com.ecommerce.notificationservice.model.NotificationStatus;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock notification dispatcher.
 * Logs the message as if sending via SMTP / SMS gateway.
 * Real integrations: swap logging for JavaMailSender / Twilio SDK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .body(request.getBody())
                .type(request.getType())
                .referenceId(request.getReferenceId())
                .build();

        try {
            // === MOCK DISPATCH ===
            log.info("[MOCK {}] To: {} | Subject: {} | RefId: {}",
                    request.getType(), request.getRecipient(),
                    request.getSubject(), request.getReferenceId());
            log.debug("[MOCK body] {}", request.getBody());
            // =====================

            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception ex) {
            log.error("Failed to send notification to {}: {}", request.getRecipient(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
        }

        return toDto(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByReferenceId(Long referenceId) {
        return notificationRepository.findByReferenceId(referenceId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).recipient(n.getRecipient()).subject(n.getSubject())
                .type(n.getType()).status(n.getStatus())
                .referenceId(n.getReferenceId()).sentAt(n.getSentAt())
                .build();
    }
}
