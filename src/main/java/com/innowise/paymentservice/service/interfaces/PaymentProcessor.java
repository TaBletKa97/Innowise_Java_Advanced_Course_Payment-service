package com.innowise.paymentservice.service.interfaces;

import com.innowise.paymentservice.repository.entity.Payment;

public interface PaymentProcessor {

    /**
     * Processes the given payment and updates its status and timestamp.
     *
     * @param payment the payment to process, must not be null and must not already have a status
     * @return the processed payment with updated status and timestamp
     */
    Payment process(Payment payment);
}
