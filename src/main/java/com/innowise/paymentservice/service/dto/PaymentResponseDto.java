package com.innowise.paymentservice.service.dto;

import com.innowise.paymentservice.repository.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        String id,
        Long orderId,
        Long userId,
        PaymentStatus status,
        LocalDateTime timestamp,
        BigDecimal paymentAmount
) {
}
