package com.innowise.paymentservice.exceptions;

public class WrongHeaderException extends RuntimeException {

    private static final String WRONG_HEADER_MSG =
            "Header must contain user id and role.";

    public WrongHeaderException() {
        super(WRONG_HEADER_MSG);
    }
}
