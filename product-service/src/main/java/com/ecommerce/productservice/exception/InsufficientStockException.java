package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ApiException {
    public InsufficientStockException(String msg) { super(msg, HttpStatus.UNPROCESSABLE_ENTITY); }
}
