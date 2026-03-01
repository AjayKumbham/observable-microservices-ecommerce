package com.ecommerce.paymentservice.exception;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends ApiException {
    public PaymentProcessingException(String msg) { super(msg, HttpStatus.UNPROCESSABLE_ENTITY); }
}
