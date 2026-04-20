package com.innowise.paymentservice.service.implementations;

import com.innowise.paymentservice.exceptions.PaymentProcessException;
import com.innowise.paymentservice.external.RandomHttpClient;
import com.innowise.paymentservice.repository.entity.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorImplTest {

    @Mock
    private RandomHttpClient randomHttpClient;

    @InjectMocks
    private PaymentProcessorImpl processor;

    @ParameterizedTest
    @CsvSource({
            "2, SUCCESS",
            "1, FAILED"
    })
    void process_shouldWorkCorrectly(String random, String status) {
        // Arrange
        Payment payment = new Payment();

        when(randomHttpClient.getRandom()).thenReturn(random);

        // Act
        Payment result = processor.process(payment);

        // Assert
        assertNotNull(result);
        assertEquals(status, result.getStatus().toString());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void process_shouldThrowException() {
        // Arrange
        Payment payment = new Payment();

        when(randomHttpClient.getRandom()).thenThrow(RuntimeException.class);

        // Act & Assert
        assertThrows(PaymentProcessException.class, () -> processor.process(payment));
    }
}