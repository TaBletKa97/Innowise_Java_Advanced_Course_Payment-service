package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.repository.entity.CreatePaymentEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CreatePaymentEventRepository extends
        MongoRepository<CreatePaymentEvent, String> {

}
