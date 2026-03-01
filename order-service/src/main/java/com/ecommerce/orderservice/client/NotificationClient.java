package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient webClient;

    public NotificationClient(WebClient.Builder builder,
                              @Value("${services.notification-service.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    public void sendNotification(NotificationRequest request) {
        try {
            webClient.post()
                    .uri("/api/v1/notifications/send")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Notification dispatched to {} for ref {}", request.getRecipient(), request.getReferenceId());
        } catch (Exception ex) {
            // Notification failure is non-fatal — order is already created
            log.error("Failed to send notification for ref {}: {}", request.getReferenceId(), ex.getMessage());
        }
    }
}
