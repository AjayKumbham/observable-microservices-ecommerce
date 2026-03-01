package com.ecommerce.cartservice.client;

import com.ecommerce.cartservice.dto.ProductResponse;
import com.ecommerce.cartservice.exception.ProductServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * WebClient-based REST client for product-service.
 * Handles 404 / service-down gracefully without crashing the cart flow.
 */
@Slf4j
@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(WebClient.Builder builder,
                         @Value("${services.product-service.url}") String productServiceUrl) {
        this.webClient = builder.baseUrl(productServiceUrl).build();
    }

    public ProductResponse getProductById(Long productId) {
        log.debug("Fetching product {} from product-service", productId);
        try {
            return webClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProductServiceException("Product not found with id: " + productId, HttpStatus.NOT_FOUND);
            }
            log.error("Error calling product-service for product {}: {}", productId, ex.getMessage());
            throw new ProductServiceException("Product service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
