package com.ecommerce.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ApiException {
    public PaymentNotFoundException(String msg) { super(msg, HttpStatus.NOT_FOUND); }
}
