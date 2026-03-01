package com.ecommerce.cartservice.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceException extends ApiException {
    public ProductServiceException(String msg, HttpStatus status) { super(msg, status); }
}
