package com.innowise.paymentservice.configuration;

import com.innowise.paymentservice.repository.PaymentRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackageClasses = PaymentRepository.class)
public class AppConfig {
}
