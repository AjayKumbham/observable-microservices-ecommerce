package com.ecommerce.cartservice.exception;

import org.springframework.http.HttpStatus;

public class CartItemNotFoundException extends ApiException {
    public CartItemNotFoundException(String msg) { super(msg, HttpStatus.NOT_FOUND); }
}
