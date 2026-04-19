package com.innowise.paymentservice.configuration;

import com.innowise.paymentservice.external.RandomHttpClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(basePackages = "com.innowise.paymentservice.external",
        types = RandomHttpClient.class)
public class HttpClientsConfiguration {
}
