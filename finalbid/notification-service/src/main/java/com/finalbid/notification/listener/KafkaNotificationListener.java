package com.finalbid.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalbid.notification.client.UserServiceClient;
import com.finalbid.notification.dto.NotificationEvent;
import com.finalbid.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!aws")
public class KafkaNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;

    public KafkaNotificationListener(NotificationService notificationService,
                                     ObjectMapper objectMapper,
                                     UserServiceClient userServiceClient) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.userServiceClient = userServiceClient;
    }

    @KafkaListener(
        topics = {"finalbid.user.registered", "finalbid.bid.placed", "finalbid.auction.ended"},
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String rawMessage) {
        try {
            JsonNode node = objectMapper.readTree(rawMessage);

            // Handle auction-service wrapped format: { "type": "...", "data": { ... } }
            String eventType;
            if (node.has("data")) {
                JsonNode data = node.get("data");
                eventType = node.path("type").asText(null);
                node = objectMapper.createObjectNode()
                    .put("eventType", eventType)
                    .setAll((com.fasterxml.jackson.databind.node.ObjectNode) data);
            } else {
                eventType = node.path("eventType").asText(null);
            }

            log.info("Received Kafka event: {}", eventType);

            if ("BID_PLACED".equals(eventType)) {
                handleBidPlaced(node);
            } else if ("AUCTION_ENDED".equals(eventType)) {
                handleAuctionEnded(node);
            } else if ("USER_REGISTERED".equals(eventType)) {
                handleUserRegistered(node);
            } else {
                log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
        }
    }

    // ── USER_REGISTERED ───────────────────────────────────────────────────────

    private void handleUserRegistered(JsonNode node) {
        String recipientEmail  = node.path("recipientEmail").asText(null);
        String username        = node.path("username").asText(null);
        String verificationLink = node.path("verificationLink").asText(null);
        UUID   referenceId     = parseUuid(node.path("referenceId").asText(null));

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("USER_REGISTERED event missing recipientEmail, skipping.");
            return;
        }

        NotificationEvent event = new NotificationEvent(
            "USER_REGISTERED", recipientEmail, referenceId, username,
            null, null, null, verificationLink, null, null, null, null, null
        );
        notificationService.handle(event);
    }

    // ── BID_PLACED (outbid notification) ──────────────────────────────────────

    private void handleBidPlaced(JsonNode node) {
        String previousBidderId = node.path("previousBidderId").asText(null);
        String auctionTitle     = node.path("auctionTitle").asText("an auction");
        BigDecimal amount       = parseBigDecimal(node.path("amount").asText(null));
        String auctionId        = node.path("auctionId").asText(null);
        UUID   referenceId      = parseUuid(node.path("bidId").asText(null));

        if (previousBidderId == null || previousBidderId.isBlank()) {
            log.info("BID_PLACED: no previousBidderId (first bid on auction), no outbid notification needed.");
            return;
        }

        UserServiceClient.UserInfo previousBidder = userServiceClient.getUser(previousBidderId);
        if (previousBidder == null) {
            log.warn("BID_PLACED: could not look up previousBidder {}, skipping outbid email.", previousBidderId);
            return;
        }

        String auctionLink = "http://localhost:8080/api/auctions/" + auctionId;

        NotificationEvent event = new NotificationEvent(
            "BID_PLACED", previousBidder.email(), referenceId, previousBidder.username(),
            auctionTitle, amount, auctionLink, null, null, null, null, null, null
        );
        log.info("Sending outbid notification to {}", previousBidder.email());
        notificationService.handle(event);
    }

    // ── AUCTION_ENDED ─────────────────────────────────────────────────────────

    private void handleAuctionEnded(JsonNode node) {
        String sellerId     = node.path("sellerId").asText(null);
        String winnerId     = node.path("winnerId").asText(null);
        String auctionTitle = node.path("auctionTitle").asText("your auction");
        BigDecimal finalPrice = parseBigDecimal(node.path("finalPrice").asText(null));
        boolean hasWinner   = "true".equalsIgnoreCase(node.path("hasWinner").asText("false"));
        UUID   referenceId  = parseUuid(node.path("auctionId").asText(null));

        // Look up seller
        UserServiceClient.UserInfo seller = userServiceClient.getUser(sellerId);
        if (seller == null) {
            log.warn("AUCTION_ENDED: could not look up seller {}", sellerId);
            return;
        }

        if (hasWinner && winnerId != null && !winnerId.isBlank()) {
            // Look up winner
            UserServiceClient.UserInfo winner = userServiceClient.getUser(winnerId);
            if (winner == null) {
                log.warn("AUCTION_ENDED: could not look up winner {}", winnerId);
            } else {
                // Notify winner
                NotificationEvent winnerEvent = new NotificationEvent(
                    "AUCTION_ENDED_WINNER", winner.email(), referenceId, winner.username(),
                    auctionTitle, null, null, null, finalPrice,
                    seller.email(), seller.username(), winner.email(), winner.username()
                );
                log.info("Sending auction-won notification to {}", winner.email());
                notificationService.handle(winnerEvent);
            }

            // Notify seller — sold
            NotificationEvent sellerEvent = new NotificationEvent(
                "AUCTION_ENDED_SELLER", seller.email(), referenceId, seller.username(),
                auctionTitle, null, null, null, finalPrice,
                seller.email(), seller.username(),
                winner != null ? winner.email() : null,
                winner != null ? winner.username() : null
            );
            log.info("Sending auction-sold notification to seller {}", seller.email());
            notificationService.handle(sellerEvent);

        } else {
            // Notify seller — no bids
            NotificationEvent noBidsEvent = new NotificationEvent(
                "AUCTION_ENDED_NO_BIDS", seller.email(), referenceId, seller.username(),
                auctionTitle, null, null, null, null,
                seller.email(), seller.username(), null, null
            );
            log.info("Sending no-bids notification to seller {}", seller.email());
            notificationService.handle(noBidsEvent);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try { return UUID.fromString(value); } catch (Exception e) { return null; }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try { return new BigDecimal(value); } catch (Exception e) { return null; }
    }
}
