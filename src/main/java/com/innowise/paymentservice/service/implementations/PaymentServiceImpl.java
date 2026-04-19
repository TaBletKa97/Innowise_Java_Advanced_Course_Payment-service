package com.innowise.paymentservice.service.implementations;

import com.innowise.paymentservice.exceptions.NoPaymentRelatedToSuchOrderException;
import com.innowise.paymentservice.exceptions.PaymentAlreadyProcessedException;
import com.innowise.paymentservice.messagebrokers.MessageBroker;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import com.innowise.paymentservice.service.dto.PaymentResponseDto;
import com.innowise.paymentservice.service.interfaces.PaymentProcessor;
import com.innowise.paymentservice.service.interfaces.PaymentService;
import com.innowise.paymentservice.service.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper mapper;
    private final PaymentRepository repository;
    private final PaymentProcessor processor;
    private final MessageBroker kafka;


    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentCreateRequestDto request) {
        Payment payment = mapper.createRequestToPayment(request);

        Payment withId = repository.save(payment);

        return mapper.entityToDto(withId);
    }

    @Override
    public PaymentResponseDto getPaymentsByOrderId(Long orderId) {
        Payment payment = repository.getByOrderId(orderId).orElseThrow(() ->
                new NoPaymentRelatedToSuchOrderException(orderId));
        return mapper.entityToDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        return mapper.entityToDto(repository.getByStatus(status));
    }

    @Override
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        return mapper.entityToDto(repository.getByUserId(userId));
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(String paymentId) {
        Payment payment = repository.findById(paymentId).orElseThrow(() ->
                new NoSuchElementException("Payment not found"));

        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new PaymentAlreadyProcessedException();
        }

        payment = processor.process(payment);

        PaymentResponseDto paymentDto =
                mapper.entityToDto(repository.save(payment));

        kafka.sendMessage(paymentDto);

        return paymentDto;
    }

    @Override
    public BigDecimal getTotalAmoundForUserByTimeRange(
            Long userId, LocalDate startDate, LocalDate endDate
    ) {
        return repository.getPaymentAmountSumByUserIdAndTimestampBetween(
                userId,
                startDate.atStartOfDay(),
                endDate.atStartOfDay().plusDays(1)
        ).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getTotalAmoundByTimeRange(
            LocalDate startDate, LocalDate endDate
    ) {
        return repository.getPaymentAmountSumByTimestampBetween(
                startDate.atStartOfDay(),
                endDate.atStartOfDay().plusDays(1)
        ).orElse(BigDecimal.ZERO);
    }
}
