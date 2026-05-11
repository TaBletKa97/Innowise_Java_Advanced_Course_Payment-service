package com.innowise.paymentservice.service.implementations;

import com.innowise.paymentservice.exceptions.NoPaymentRelatedToSuchOrderException;
import com.innowise.paymentservice.exceptions.PaymentAlreadyProcessedException;
import com.innowise.paymentservice.repository.CreatePaymentEventRepository;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import com.innowise.paymentservice.service.dto.PaymentResponseDto;
import com.innowise.paymentservice.service.interfaces.PaymentProcessor;
import com.innowise.paymentservice.service.mappers.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private CreatePaymentEventRepository eventRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPayment_ShouldCreateAndReturnPaymentResponseDto() {
        // Arrange
        PaymentCreateRequestDto request = new PaymentCreateRequestDto(
                1L,
                2L,
                BigDecimal.TEN
        );

        Payment paymentEntity = new Payment();
        paymentEntity.setOrderId(1L);
        paymentEntity.setUserId(2L);
        paymentEntity.setPaymentAmount(BigDecimal.TEN);

        Payment savedPayment = new Payment();
        savedPayment.setId("1L");
        savedPayment.setOrderId(1L);
        savedPayment.setUserId(2L);
        savedPayment.setPaymentAmount(BigDecimal.TEN);

        PaymentResponseDto expectedResponse = new PaymentResponseDto(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getStatus(),
                savedPayment.getTimestamp(),
                savedPayment.getPaymentAmount()
        );

        when(paymentMapper.createRequestToPayment(request)).thenReturn(paymentEntity);
        when(paymentRepository.save(paymentEntity)).thenReturn(savedPayment);
        when(paymentMapper.entityToDto(savedPayment)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.createPayment(request);

        // Assert
        assertNotNull(result);

        verify(paymentRepository).save(paymentEntity);
        verify(paymentMapper).createRequestToPayment(request);
        verify(paymentMapper).entityToDto(savedPayment);
    }

    @Test
    void getPaymentsByOrderId_shouldReturnPaymentResponseDto() {
        // Arrange
        Long orderId = 1L;
        Payment payment = new Payment();
        payment.setId("1L");
        payment.setOrderId(1L);
        payment.setUserId(2L);
        payment.setPaymentAmount(BigDecimal.TEN);

        PaymentResponseDto expectedResponse = new PaymentResponseDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getTimestamp(),
                payment.getPaymentAmount()
        );
        when(paymentRepository.getByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentMapper.entityToDto(payment)).thenReturn(expectedResponse);

        // Act
        PaymentResponseDto result = paymentService.getPaymentsByOrderId(orderId);

        // Assert
        assertNotNull(result);

        verify(paymentRepository).getByOrderId(orderId);
        verify(paymentMapper).entityToDto(payment);
    }

    @Test
    void getPaymentsByOrderId_shouldThrowException_WhenPaymentIsNotFound() {
        // Arrange
        Long orderId = 1L;

        when(paymentRepository.getByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoPaymentRelatedToSuchOrderException.class,
                () -> paymentService.getPaymentsByOrderId(orderId));
    }

    @Test
    void getPaymentsByStatus_shouldReturnListOfPaymentResponseDto() {
        // Arrange
        var status = PaymentStatus.SUCCESS;
        var payment = new Payment();
        var responseDto = new PaymentResponseDto(
                "1L",
                1L,
                1L,
                PaymentStatus.SUCCESS,
                LocalDateTime.now(),
                BigDecimal.TEN
        );
        var paymentList = List.of(payment);
        var responceList = List.of(responseDto);

        when(paymentRepository.getByStatus(status)).thenReturn(paymentList);
        when(paymentMapper.entityToDto(paymentList)).thenReturn(responceList);

        // Act
        List<PaymentResponseDto> result = paymentService.getPaymentsByStatus(status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(paymentRepository).getByStatus(status);
        verify(paymentMapper).entityToDto(paymentList);
    }

    @Test
    void getPaymentsByUserId_shouldReturnListOfPaymentResponseDto() {
        // Arrange
        Long userId = 1L;
        var payment = new Payment();
        var responseDto = new PaymentResponseDto(
                "1L",
                1L,
                1L,
                PaymentStatus.SUCCESS,
                LocalDateTime.now(),
                BigDecimal.TEN
        );
        var paymentList = List.of(payment);
        var responceList = List.of(responseDto);

        when(paymentRepository.getByUserId(userId)).thenReturn(paymentList);
        when(paymentMapper.entityToDto(paymentList)).thenReturn(responceList);
        // Act
        var result = paymentService.getPaymentsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(paymentRepository).getByUserId(userId);
        verify(paymentMapper).entityToDto(paymentList);
    }

    @Test
    void processPayment_shouldReturnPaymentResponseDto() {
        // Arrange
        var paymentId = "1L";
        var payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        var response = new PaymentResponseDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getStatus(),
                payment.getTimestamp(),
                payment.getPaymentAmount()
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentProcessor.process(payment)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.entityToDto(payment)).thenReturn(response);
        // Act
        var result = paymentService.processPayment(paymentId);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(paymentId);
        verify(paymentProcessor).process(payment);
        verify(paymentMapper).entityToDto(payment);
        eventRepository.save(any());
    }

    @Test
    void processPayment_shouldThrowException_WhenPaymentIsAlreadyProcessed() {
        // Arrange
        var paymentId = "UUID.randomUUID()";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NoSuchElementException.class, () ->
                paymentService.processPayment(paymentId));
    }

    @Test
    void processPayment_shouldThrowException_WhenPaymentIsNotExists() {
        // Arrange
        var paymentId = "UUID.randomUUID()";
        var payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(PaymentAlreadyProcessedException.class, () ->
                paymentService.processPayment(paymentId));
    }

    @Test
    void getTotalAmoundForUserByTimeRange_ShouldReturnAmount() {
        // Arrange
        Long userId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        when(paymentRepository.getPaymentAmountSumByUserIdAndTimestampBetween(
                eq(userId), any(), any())
        ).thenReturn(Optional.of(BigDecimal.ONE));
        // Act
        var result = paymentService.getTotalAmoundForUserByTimeRange(
                userId, startDate, endDate);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).getPaymentAmountSumByUserIdAndTimestampBetween(
                any(), any(), any()
        );
    }

    @Test
    void getTotalAmoundByTimeRange_ShouldReturnAmount() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        when(paymentRepository.getPaymentAmountSumByTimestampBetween(
                any(), any())
        ).thenReturn(Optional.of(BigDecimal.ONE));
        // Act
        var result = paymentService.getTotalAmoundByTimeRange(
                startDate, endDate);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).getPaymentAmountSumByTimestampBetween(
                any(), any()
        );
    }
}