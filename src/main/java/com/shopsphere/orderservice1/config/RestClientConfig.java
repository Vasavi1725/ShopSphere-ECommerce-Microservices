package com.shopsphere.orderservice1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("productRestClient")
    public RestClient productRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

    @Bean("paymentRestClient")
    public RestClient paymentRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }
}
