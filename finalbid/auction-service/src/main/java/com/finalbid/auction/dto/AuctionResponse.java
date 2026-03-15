package com.finalbid.auction.dto;

import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.model.Condition;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuctionResponse(
    UUID id,
    UUID sellerId,
    String title,
    String description,
    Category category,
    Condition condition,
    BigDecimal startPrice,
    BigDecimal currentPrice,
    int bidCount,
    Instant endAt,
    AuctionStatus status,
    String imageUrl,
    UUID winnerId,
    Instant createdAt
) {}
