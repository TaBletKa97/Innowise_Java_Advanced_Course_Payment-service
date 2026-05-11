package com.innowise.paymentservice.exceptions;

public class PaymentProcessException extends RuntimeException {

    private static final String MSG = "Processing payment with id: %s was failed.\n";

    public PaymentProcessException(String paymentId, Exception e) {
        super(MSG.formatted(paymentId), e);
    }
}
