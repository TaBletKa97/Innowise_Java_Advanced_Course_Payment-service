package com.innowise.paymentservice.service.interfaces;

import com.innowise.paymentservice.exceptions.NoPaymentRelatedToSuchOrderException;
import com.innowise.paymentservice.exceptions.PaymentProcessException;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import com.innowise.paymentservice.service.dto.PaymentResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    /**
     * Creates a new payment based on the provided request data.
     * @param request the request containing payment details such as order ID,
     * user ID, and payment amount; must not be null
     * @return the created payment with assigned ID, timestamp, and initial status
     */
    PaymentResponseDto createPayment(PaymentCreateRequestDto request);

    /**
     * Retrieves the payment associated with the specified order ID.
     *
     * @param orderId the ID of the order for which to retrieve the payment;
     * must not be null
     * @return the payment response data transfer object containing details such
     * as payment ID, order ID, user ID, status, timestamp, and amount;
     * @throws NoPaymentRelatedToSuchOrderException when there is no payment
     * related to provided order id
     */
    PaymentResponseDto getPaymentsByOrderId(Long orderId);

    /**
     * Retrieves a list of payments filtered by the specified status.
     *
     * @param status the payment status to filter by; must not be null
     * @return a list of payment response data transfer objects matching the
     * given status;
     * may be empty if no payments exist with the provided status
     */
    List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status);

    /**
     * Retrieves a list of payments associated with the specified user ID.
     *
     * @param userId the ID of the user whose payments are to be retrieved;
     * must not be null
     * @return a list of payment response data transfer objects filtered by the
     * user ID;
     * may be empty if no payments exist for the given user ID
     */
    List<PaymentResponseDto> getPaymentsByUserId(Long userId);

    /**
     * Processes the payment with the specified ID by initiating its processing
     * logic, updating its status and timestamp accordingly.
     *
     * @param paymentId the unique identifier of the payment to process;
     * must not be null
     * @return the updated payment response data transfer object reflecting the
     * new status and timestamp
     * @throws PaymentProcessException when an error occurs during processing
     */
    PaymentResponseDto processPayment(String paymentId);


    /**
     * Calculates the total payment amount for a specific user within a given time range.
     *
     * @param userId the ID of the user; must not be null
     * @param startDate the start date of the time range (inclusive); must not be null
     * @param endDate the end date of the time range (inclusive); must not be null
     * @return the total payment amount for the user within the specified time range;
     *         zero or positive value, or null if no payments exist in the range
     */
    BigDecimal getTotalAmoundForUserByTimeRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculates the total payment amount across all users within the specified time range.
     *
     * @param startDate the start date of the time range (inclusive); must not be null
     * @param endDate the end date of the time range (inclusive); must not be null
     * @return the total payment amount for all users within the specified time range;
     *         zero or positive value, or null if no payments exist in the range
     */
    BigDecimal getTotalAmoundByTimeRange(LocalDate startDate, LocalDate endDate);


}
