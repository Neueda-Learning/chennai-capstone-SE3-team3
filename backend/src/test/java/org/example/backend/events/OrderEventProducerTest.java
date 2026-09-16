package org.example.backend.events;

import org.example.backend.enums.OrderSide;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, OrderPlacedMessage> kafkaTemplate;

    @Test
    void publishesOrderPlacedToOrdersTopicKeyedByAccount() {
        OrderEventProducer producer = new OrderEventProducer(kafkaTemplate, "orders");

        OrderPlacedEvent event = new OrderPlacedEvent(
                9001L,
                1,
                "ACME",
                OrderSide.BUY,
                100L,
                OffsetDateTime.parse("2026-09-16T09:00:00Z"));

        producer.publishOrderPlaced(event);

        ArgumentCaptor<OrderPlacedMessage> payloadCaptor =
                ArgumentCaptor.forClass(OrderPlacedMessage.class);

        verify(kafkaTemplate).send(
                eq("orders"),
                eq("1"),
                payloadCaptor.capture());

        assertEquals("ORDER_PLACED", payloadCaptor.getValue().eventType());
        assertEquals(9001L, payloadCaptor.getValue().orderId());
        assertEquals(1, payloadCaptor.getValue().accountId());
    }
}


