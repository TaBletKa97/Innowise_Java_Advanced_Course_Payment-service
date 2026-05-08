package com.innowise.paymentservice.messagebrokers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaOrderEventProducer implements MessageBroker {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public boolean sendMessage(Object message) {
        try {
            kafkaTemplate.send("CREATE_PAYMENT", message).get(5, TimeUnit.SECONDS);
            log.debug("Payment {} was sent to Kafka", message);
            return true;

        } catch (InterruptedException e) {
            log.info("Failed to send message to Kafka.\n", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("Failed to send message to Kafka.\n", e);
            return false;
        }
    }
}
