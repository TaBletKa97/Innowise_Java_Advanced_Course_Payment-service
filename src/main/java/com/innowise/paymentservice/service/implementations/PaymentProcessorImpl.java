package com.innowise.paymentservice.service.implementations;

import com.innowise.paymentservice.exceptions.PaymentProcessException;
import com.innowise.paymentservice.external.RandomHttpClient;
import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.interfaces.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentProcessorImpl implements PaymentProcessor {

    private final RandomHttpClient random;

    @Override
    public Payment process(Payment payment) {
        int digit;
        PaymentStatus status;

        log.debug("Processing payment with id: {}", payment.getId());

        try {
            digit = Integer.parseInt(random.getRandom().strip());
        } catch (Exception e) {
            throw new PaymentProcessException(payment.getId(), e);
        }

        status = (digit % 2 == 1) ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;
        payment.setStatus(status);
        payment.setTimestamp(LocalDateTime.now());

        log.debug("Payment {} processed.", payment);

        return  payment;
    }
}
