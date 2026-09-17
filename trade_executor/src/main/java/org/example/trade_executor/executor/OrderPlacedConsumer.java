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

import java.util.Objects;

@Component
public class OrderPlacedConsumer {

    static final String ORDER_PLACED_EVENT_TYPE = "ORDER_PLACED";

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
                throw new PoisonOrderMessageException("missing payload");
            }

            if (!ORDER_PLACED_EVENT_TYPE.equals(Objects.toString(envelope.eventType(), null))) {
                throw new PoisonOrderMessageException("unexpected eventType: " + envelope.eventType());
            }

            if (envelope.getPayload().orderId() <= 0) {
                throw new PoisonOrderMessageException("missing orderId");
            }

            return envelope;
        } catch (JsonProcessingException ex) {
            throw new PoisonOrderMessageException("malformed JSON", ex);
        }
    }
}

