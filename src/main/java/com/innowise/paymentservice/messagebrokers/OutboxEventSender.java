package com.innowise.paymentservice.messagebrokers;

import com.innowise.paymentservice.repository.CreatePaymentEventRepository;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.entity.CreatePaymentEvent;
import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.service.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
public class OutboxEventSender {

    private final MessageBroker messageBroker;
    private final CreatePaymentEventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper mapper;

    @Scheduled(fixedRate = 1000)
    private void sendMessages() {
        List<CreatePaymentEvent> all = eventRepository.findAll();

        if (all.isEmpty()) {
            return;
        }

        log.info("Starting event processing:");

        List<Payment> payments = paymentRepository.findAllById(
                all.stream().map(CreatePaymentEvent::getPaymentId).toList()
        );

        for (CreatePaymentEvent event : all) {
            var payment = payments.stream()
                    .filter(p -> p.getId().equals(event.getPaymentId()))
                    .findFirst()
                    .get();

            if (messageBroker.sendMessage(mapper.entityToDto(payment))) {
                eventRepository.delete(event);
            } else {
                log.info("Event failed to send. Stopping event processing.");
                break;
            }
            log.info("Event {} was processed", event);
        }
    }
}
