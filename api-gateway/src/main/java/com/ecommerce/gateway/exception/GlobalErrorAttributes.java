package com.ecommerce.gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> map = super.getErrorAttributes(request, options);
        
        Throwable error = getError(request);
        if (error instanceof ResponseStatusException rse) {
            map.put("message", rse.getReason() != null ? rse.getReason() : rse.getMessage());
            map.put("status", rse.getStatusCode().value());
        } else if (error != null) {
            map.put("message", error.getMessage());
        }

        map.put("timestamp", LocalDateTime.now());
        map.remove("requestId");
        map.remove("path");
        
        return map;
    }
}
