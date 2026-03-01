package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends ApiException {
    public CategoryAlreadyExistsException(String msg) { super(msg, HttpStatus.CONFLICT); }
}
