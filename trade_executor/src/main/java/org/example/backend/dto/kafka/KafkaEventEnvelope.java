package org.example.backend.dto.kafka;

public record KafkaEventEnvelope<T>(
        String eventId,
        String eventType,
        String occurredAt,
        String source,
        int schemaVersion,
        T payload) {

    public T getPayload() {
        return payload;
    }
}


