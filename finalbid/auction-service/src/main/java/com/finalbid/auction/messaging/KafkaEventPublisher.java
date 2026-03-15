package com.finalbid.auction.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("default")
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, String eventType, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", eventType);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("data", data);

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish Kafka event", e);
        }
    }
}
