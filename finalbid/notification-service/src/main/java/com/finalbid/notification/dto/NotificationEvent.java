package com.finalbid.notification.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record NotificationEvent(
    String eventType,
    String recipientEmail,
    UUID referenceId,
    String username,
    String auctionTitle,
    BigDecimal currentPrice,
    String auctionLink,
    String verificationLink,
    BigDecimal finalPrice,
    String sellerEmail,
    String sellerUsername,
    String winnerEmail,
    String winnerUsername
) {}
