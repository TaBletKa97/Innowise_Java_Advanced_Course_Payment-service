package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import com.innowise.paymentservice.service.dto.PaymentResponseDto;
import com.innowise.paymentservice.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a new payment.
     *
     * @param createRequest the request containing payment details including
     * order ID, user ID, and payment amount; must not be null and must be valid
     * @return the created payment response with assigned ID, timestamp, and
     * initial status
     */
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody @Valid PaymentCreateRequestDto createRequest
    ) {
        return ResponseEntity.ok(paymentService.createPayment(createRequest));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/users/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPaymentByUserId(#id)")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(id));
    }

    @GetMapping("/orders/{id}/payment")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPaymentByOrderId(#id)")
    public ResponseEntity<PaymentResponseDto> getPaymentsByOrderId(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(id));
    }


    @PatchMapping("/payments/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPayment(#id)")
    public ResponseEntity<PaymentResponseDto> processPaymentsById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(paymentService.processPayment(id));
    }

    @GetMapping("/users/{id}/payments/total")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPaymentByUserId(#id)")
    public ResponseEntity<BigDecimal> getTotalByUserId(
            @PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(paymentService.getTotalAmoundForUserByTimeRange(
                id, startDate, endDate)
        );
    }

    @GetMapping("/payments/total")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<BigDecimal> getTotal(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(paymentService.getTotalAmoundByTimeRange(
                startDate, endDate)
        );
    }

}
