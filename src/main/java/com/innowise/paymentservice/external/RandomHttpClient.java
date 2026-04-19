package com.innowise.paymentservice.external;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(
        url = "${random-service.uri}"
)
public interface RandomHttpClient {

    @GetExchange
    Integer getRandom();
}
