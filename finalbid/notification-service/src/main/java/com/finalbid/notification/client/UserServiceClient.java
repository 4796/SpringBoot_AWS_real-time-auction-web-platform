package com.finalbid.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for looking up user email/username from user-service internal endpoint.
 * Used to enrich Kafka events that only contain user UUIDs.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${app.user-service-url:http://user-service:8081}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    /**
     * Looks up a user by UUID. Returns null if not found or on error.
     */
    public UserInfo getUser(String userId) {
        if (userId == null || userId.isBlank()) return null;
        try {
            String url = userServiceUrl + "/internal/users/" + userId;
            UserInfo info = restTemplate.getForObject(url, UserInfo.class);
            return info;
        } catch (Exception e) {
            log.warn("Failed to fetch user info for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    public record UserInfo(String email, String username) {}
}
