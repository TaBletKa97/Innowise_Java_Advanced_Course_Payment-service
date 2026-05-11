package com.innowise.paymentservice.service.mappers;

import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import com.innowise.paymentservice.service.dto.PaymentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface PaymentMapper {

    PaymentResponseDto entityToDto(Payment payment);

    List<PaymentResponseDto> entityToDto(List<Payment> payments);

    @Mapping(target = "status", expression = "java(PaymentStatus.PENDING)")
    Payment createRequestToPayment(PaymentCreateRequestDto request);
}
