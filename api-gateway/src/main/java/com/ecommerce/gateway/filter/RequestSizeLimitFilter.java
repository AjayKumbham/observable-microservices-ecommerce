package com.ecommerce.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global filter that rejects requests exceeding the configured size limit
 * and returns a structured JSON error body consistent with the microservice
 * error response schema: { status, message, timestamp, errors }.
 *
 * This replaces the default Spring Cloud Gateway RequestSize filter which
 * writes an empty 413 response directly to the wire, bypassing error handlers.
 */
@Slf4j
@Component
public class RequestSizeLimitFilter implements GlobalFilter, Ordered {

    // 10KB for auth endpoints, 100KB for everything else
    private static final long AUTH_MAX_BYTES = 10 * 1024L;
    private static final long DEFAULT_MAX_BYTES = 100 * 1024L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        // Run very early, before routing filters (which are at Integer.MIN_VALUE + 1)
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        long maxSize = path.startsWith("/api/v1/auth/") ? AUTH_MAX_BYTES : DEFAULT_MAX_BYTES;

        String contentLengthHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLengthHeader != null) {
            long contentLength;
            try {
                contentLength = Long.parseLong(contentLengthHeader);
            } catch (NumberFormatException e) {
                return chain.filter(exchange); // Not a valid number — let it pass, Netty will handle it
            }

            if (contentLength > maxSize) {
                log.warn("Rejected oversized request: path={}, size={}B, limit={}B", path, contentLength, maxSize);
                return writeErrorResponse(exchange, maxSize);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, long maxSize) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        body.put("message", String.format(
                "Request payload exceeds the maximum allowed size of %dKB for this endpoint.",
                maxSize / 1024));
        body.put("timestamp", LocalDateTime.now()
                .truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
                .toString());
        body.put("errors", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"status\":413,\"message\":\"Request too large\"}".getBytes();
        }

        exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().setContentLength(bytes.length);

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
