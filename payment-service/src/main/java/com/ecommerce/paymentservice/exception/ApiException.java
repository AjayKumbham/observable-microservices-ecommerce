package com.ecommerce.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(String msg, HttpStatus status) { super(msg); this.status = status; }
}
