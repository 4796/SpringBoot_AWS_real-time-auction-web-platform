package com.finalbid.auction.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("aws")
public class SnsEventPublisher implements EventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    public SnsEventPublisher(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topicArn, String eventType, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", eventType);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("data", data);

            String json = objectMapper.writeValueAsString(payload);
            
            PublishRequest request = PublishRequest.builder()
                .message(json)
                .topicArn(topicArn)
                .build();

            snsClient.publish(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish SNS event", e);
        }
    }
}
