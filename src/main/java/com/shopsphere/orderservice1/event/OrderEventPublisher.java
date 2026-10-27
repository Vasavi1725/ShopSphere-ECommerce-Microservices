package com.shopsphere.orderservice1.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderConfirmed(Long orderId) {
        kafkaTemplate.send(
            "order-confirmed",
            String.valueOf(orderId),
            "{\"eventType\":\"ORDER_CONFIRMED\",\"orderId\":" + orderId + "}"
        );
    }
}
