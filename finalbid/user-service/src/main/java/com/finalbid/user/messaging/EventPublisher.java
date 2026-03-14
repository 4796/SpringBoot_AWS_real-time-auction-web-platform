package com.finalbid.user.messaging;

/**
 * Common event publisher interface.
 * Implementations: KafkaEventPublisher (default profile),
 *                  SnsEventPublisher (aws profile).
 */
public interface EventPublisher {

    /**
     * Publish a USER_REGISTERED event.
     *
     * @param userId            the new user's UUID
     * @param email             recipient email
     * @param username          display name
     * @param verificationToken email verification UUID token
     * @param verificationLink  full clickable link
     */
    void publishUserRegistered(String userId,
                               String email,
                               String username,
                               String verificationToken,
                               String verificationLink);
}
