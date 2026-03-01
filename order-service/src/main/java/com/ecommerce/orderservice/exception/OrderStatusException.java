package com.ecommerce.orderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderStatusException extends ApiException {
    public OrderStatusException(String msg) { super(msg, HttpStatus.UNPROCESSABLE_ENTITY); }
}
