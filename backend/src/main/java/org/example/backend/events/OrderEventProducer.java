package org.example.backend.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderPlacedMessage> kafkaTemplate;
    private final String ordersTopic;

    public OrderEventProducer(
            KafkaTemplate<String, OrderPlacedMessage> kafkaTemplate,
            @Value("${trading.kafka.orders-topic:orders}") String ordersTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.ordersTopic = ordersTopic;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send(
                ordersTopic,
                String.valueOf(event.accountId()),
                OrderPlacedMessage.from(event));
    }
}

