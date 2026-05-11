package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> getByUserId(Long userId);

    Optional<Payment> getByOrderId(Long orderId);

    List<Payment> getByStatus(PaymentStatus status);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId': ?0, 'timestamp': { '$gte': ?1, '$lte': ?2 }, 'status' : { '$eq': 'SUCCESS' } } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$paymentAmount' } } }"
    })
    Optional<BigDecimal> getPaymentAmountSumByUserIdAndTimestampBetween(
            Long userId, LocalDateTime timestamp, LocalDateTime timestamp2);

    @Aggregation(pipeline = {
            "{ '$match': { 'timestamp': { '$gte': ?0, '$lte': ?1 }, 'status' : { '$eq': 'SUCCESS' }  } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$paymentAmount' } } }"
    })
    Optional<BigDecimal> getPaymentAmountSumByTimestampBetween(
            LocalDateTime timestamp, LocalDateTime timestamp2);
}
