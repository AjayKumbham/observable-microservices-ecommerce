package com.ecommerce.productservice.exception;

import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends ApiException {
    public CategoryNotFoundException(String msg) { super(msg, HttpStatus.NOT_FOUND); }
}
