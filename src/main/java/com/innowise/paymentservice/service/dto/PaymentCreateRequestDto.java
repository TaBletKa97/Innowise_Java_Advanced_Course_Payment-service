package com.innowise.paymentservice.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentCreateRequestDto(
        @NotNull
        @Positive
        Long orderId,

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        BigDecimal paymentAmount
) {
}
