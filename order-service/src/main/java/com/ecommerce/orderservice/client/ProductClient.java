package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(WebClient.Builder builder,
                         @Value("${services.product-service.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    public ProductResponse getProductById(Long productId) {
        try {
            return webClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Product not found: " + productId);
            }
            log.error("product-service error: {}", ex.getMessage());
            throw new RuntimeException("Product service unavailable");
        }
    }

    public void decrementStock(Long productId, int quantity) {
        try {
            webClient.patch()
                    .uri("/api/v1/products/{id}/inventory", productId)
                    .bodyValue(java.util.Map.of("quantity", -quantity, "mode", "DELTA"))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Decremented stock for product {} by {}", productId, quantity);
        } catch (Exception ex) {
            log.error("Failed to decrement stock for product {}: {}", productId, ex.getMessage());
            // Non-fatal — compensate via scheduled job in production
        }
    }
}
