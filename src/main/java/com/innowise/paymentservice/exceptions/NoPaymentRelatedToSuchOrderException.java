package com.innowise.paymentservice.exceptions;

public class NoPaymentRelatedToSuchOrderException extends RuntimeException {

    private static final String MSG = "Payment for order with id %d was not found.";

    public NoPaymentRelatedToSuchOrderException(long id) {
        super(MSG.formatted(id));
    }
}
