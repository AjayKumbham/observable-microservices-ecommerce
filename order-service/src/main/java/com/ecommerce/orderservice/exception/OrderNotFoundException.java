package com.ecommerce.orderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends ApiException {
    public OrderNotFoundException(String msg) { super(msg, HttpStatus.NOT_FOUND); }
}
