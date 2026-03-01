package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.PaymentRequest;
import com.ecommerce.orderservice.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder builder,
                         @Value("${services.payment-service.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    public PaymentResponse chargePayment(PaymentRequest request) {
        log.info("Calling payment-service for order {}", request.getOrderId());
        return webClient.post()
                .uri("/api/v1/payments/charge")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();
    }
}
