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

    /**
     * Retrieves a list of payments filtered by the specified status.
     *
     * @param status the payment status to filter by; must not be null
     * @return a list of payment response DTOs with the specified status;
     *         may be empty if no payments exist with the given status
     */
    @GetMapping("/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    /**
     * Retrieves a list of payments associated with the specified user ID.
     *
     * @param id the ID of the user whose payments are to be retrieved;
     * must not be null
     * @return a list of payment response DTOs associated with the user;
     * may be empty if no payments exist for the given user ID
     */
    @GetMapping("/users/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPaymentByUserId(#id)")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(id));
    }

    /**
     * Retrieves the payment associated with the specified order ID.
     *
     * @param id the ID of the order for which to retrieve the payment;
     * must not be null
     * @return the payment response DTO containing details such as payment ID,
     * order ID, user ID, status, timestamp, and amount;
     * never null when successful
     */
    @GetMapping("/orders/{id}/payment")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPaymentByOrderId(#id)")
    public ResponseEntity<PaymentResponseDto> getPaymentsByOrderId(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(id));
    }


    /**
     * Processes the payment identified by the given ID.
     *
     * @param id the ID of the payment to be processed; must not be null
     * @return the updated payment response DTO with the new status and related
     * details
     */
    @PatchMapping("/payments/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @ssi.canAccessPayment(#id)")
    public ResponseEntity<PaymentResponseDto> processPaymentsById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(paymentService.processPayment(id));
    }

    /**
     * Retrieves the total payment amount for a specific user within a given
     * date range.
     *
     * @param id the ID of the user whose total payment amount is to be
     * calculated; must not be null
     * @param startDate the start date of the time range (inclusive);
     * must not be null
     * @param endDate the end date of the time range (inclusive);
     * must not be null
     * @return the total payment amount as a non-null BigDecimal for the
     * specified user and time range
     */
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

    /**
     * Retrieves the total payment amount across all users within a given date range.
     *
     * @param startDate the start date of the time range (inclusive);
     * must not be null
     * @param endDate the end date of the time range (inclusive);
     * must not be null
     * @return the total payment amount as a non-null BigDecimal for the
     * specified time range
     */
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
