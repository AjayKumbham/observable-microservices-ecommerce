package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class ProductAlreadyExistsException extends ApiException {
    public ProductAlreadyExistsException(String msg) { super(msg, HttpStatus.CONFLICT); }
}
