package com.innowise.paymentservice.repository.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document
@Setter
@Getter
@ToString
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long orderId;

    @Indexed
    private Long userId;

    @Indexed
    private PaymentStatus status;

    @Indexed
    private LocalDateTime timestamp;

    private BigDecimal paymentAmount;

}
