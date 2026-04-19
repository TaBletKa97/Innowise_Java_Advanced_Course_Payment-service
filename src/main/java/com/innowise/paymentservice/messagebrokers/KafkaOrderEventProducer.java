package com.innowise.paymentservice.messagebrokers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class KafkaOrderEventProducer implements MessageBroker {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendMessage(Object message) {
        kafkaTemplate.send("CREATE_PAYMENT", message);
        log.debug("Payment {} was sent to kafka", message);
    }
}
