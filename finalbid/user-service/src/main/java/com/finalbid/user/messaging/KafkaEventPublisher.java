package com.finalbid.user.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka-based event publisher. Active on the "default" profile (local Docker Compose).
 * Topics from spec Section 7.1.
 */
@Component
@Profile("!aws")
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Value("${kafka.topic.user-registered}")
    private String userRegisteredTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper                  objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    @Override
    public void publishUserRegistered(String userId,
                                      String email,
                                      String username,
                                      String verificationToken,
                                      String verificationLink) {
        try {
            Map<String, String> payload = Map.of(
                "eventType",         "USER_REGISTERED",
                "userId",            userId,
                "referenceId",       userId,
                "recipientEmail",    email,
                "username",          username,
                "verificationToken", verificationToken,
                "verificationLink",  verificationLink
            );
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(userRegisteredTopic, userId, json);
            log.info("EVENT_PUBLISHED topic={} userId={}", userRegisteredTopic, userId);
        } catch (Exception e) {
            log.error("EVENT_PUBLISH_FAILED topic={} userId={} error={}",
                      userRegisteredTopic, userId, e.getMessage(), e);
        }
    }
}
