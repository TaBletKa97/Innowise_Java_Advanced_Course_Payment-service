package com.innowise.paymentservice.security;

import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("ssi")
@RequiredArgsConstructor
public class SecurityServiceImpl {

    private final PaymentRepository repository;

    public boolean canAccessPaymentByUserId(Long userId) {
        Long loggedUserId = (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userId.equals(loggedUserId);
    }

    public boolean canAccessPaymentByOrderId(Long orderId) {
        Long loggedUserId = (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<Payment> payment = repository.getByOrderId(orderId);

        return payment.map(p -> p.getUserId().equals(loggedUserId))
                .orElse(false);

    }

    public boolean canAccessPayment(String paymentId) {
        Long loggedUserId = (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<Payment> payment = repository.findById(paymentId);
        return payment.map(p -> p.getUserId().equals(loggedUserId))
                .orElse(false);
    }
}
