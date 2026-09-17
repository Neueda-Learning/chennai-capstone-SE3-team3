package org.example.trade_executor.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.dto.kafka.KafkaEventEnvelope;
import org.example.backend.events.OrderPlacedMessage;
import org.example.trade_executor.service.TradeExecutorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private static final TypeReference<KafkaEventEnvelope<OrderPlacedMessage>> ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;
    private final TradeExecutorService tradeExecutorService;

    public OrderPlacedConsumer(
            ObjectMapper objectMapper,
            TradeExecutorService tradeExecutorService) {

        this.objectMapper = objectMapper;
        this.tradeExecutorService = tradeExecutorService;
    }

    @KafkaListener(
            topics = "${trading.kafka.orders-topic:orders}",
            groupId = "trade-executor",
            containerFactory = "ordersKafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment acknowledgment) {
        tradeExecutorService.execute(parse(message).getPayload());
        acknowledgment.acknowledge();
    }

    KafkaEventEnvelope<OrderPlacedMessage> parse(String message) {
        try {
            KafkaEventEnvelope<OrderPlacedMessage> envelope = objectMapper.readValue(message, ENVELOPE_TYPE);
            if (envelope.getPayload() == null) {
                throw new IllegalArgumentException("Kafka envelope payload is required");
            }
            return envelope;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to parse ORDER_PLACED message", ex);
        }
    }
}

