package com.innowise.paymentservice.exceptions;

public class PaymentAlreadyProcessedException extends RuntimeException {

    private static final String MSG = "Payment already processed";

    public PaymentAlreadyProcessedException() {
        super(MSG);
    }
}
