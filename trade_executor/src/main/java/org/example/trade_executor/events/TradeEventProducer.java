package org.example.trade_executor.events;

import org.example.backend.dto.kafka.KafkaEventEnvelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class TradeEventProducer {

    private final KafkaTemplate<String, KafkaEventEnvelope<OrderResolvedMessage>> kafkaTemplate;
    private final String tradesTopic;
    private final String source;
    private final int schemaVersion;

    public TradeEventProducer(
            KafkaTemplate<String, KafkaEventEnvelope<OrderResolvedMessage>> kafkaTemplate,
            @Value("${trading.kafka.trades-topic:trades}") String tradesTopic,
            @Value("${trading.kafka.execution-source:trade-executor}") String source,
            @Value("${trading.kafka.schema-version:1}") int schemaVersion) {

        this.kafkaTemplate = kafkaTemplate;
        this.tradesTopic = tradesTopic;
        this.source = source;
        this.schemaVersion = schemaVersion;
    }

    public void publish(OrderResolvedEvent event) {
        KafkaEventEnvelope<OrderResolvedMessage> envelope = new KafkaEventEnvelope<>(
                UUID.randomUUID().toString(),
                event.eventType(),
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                source,
                schemaVersion,
                OrderResolvedMessage.from(event));

        kafkaTemplate.send(
                tradesTopic,
                String.valueOf(event.accountId()),
                envelope);
    }
}

