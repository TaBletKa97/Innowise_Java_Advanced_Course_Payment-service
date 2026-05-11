package com.innowise.paymentservice.repository.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Setter
@Getter
@ToString
public class CreatePaymentEvent {

    @Id
    private String id;

    private String paymentId;

    private LocalDateTime timestamp;

    public CreatePaymentEvent(String paymentId, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.timestamp = timestamp;
    }
}
