package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApiException {
    public ProductNotFoundException(String msg) { super(msg, HttpStatus.NOT_FOUND); }
}
