package com.finalbid.user.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SNS-based event publisher. Active on the "aws" profile (Amazon SNS).
 * Topic ARNs from spec Section 7.2 / environment variables.
 */
@Component
@Profile("aws")
public class SnsEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SnsEventPublisher.class);

    @Value("${sns.user-registered-arn}")
    private String userRegisteredArn;

    private final SnsTemplate  snsTemplate;
    private final ObjectMapper objectMapper;

    public SnsEventPublisher(SnsTemplate snsTemplate, ObjectMapper objectMapper) {
        this.snsTemplate  = snsTemplate;
        this.objectMapper = objectMapper;
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
                "recipientEmail",    email,
                "username",          username,
                "verificationToken", verificationToken,
                "verificationLink",  verificationLink
            );
            String json = objectMapper.writeValueAsString(payload);
            snsTemplate.sendNotification(userRegisteredArn, json, "USER_REGISTERED");
            log.info("EVENT_PUBLISHED sns={} userId={}", userRegisteredArn, userId);
        } catch (Exception e) {
            log.error("EVENT_PUBLISH_FAILED sns={} userId={} error={}",
                      userRegisteredArn, userId, e.getMessage(), e);
        }
    }
}
