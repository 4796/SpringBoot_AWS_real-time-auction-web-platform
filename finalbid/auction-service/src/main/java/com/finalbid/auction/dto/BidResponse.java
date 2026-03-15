package com.finalbid.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BidResponse(
    UUID id,
    UUID auctionId,
    UUID bidderId,
    BigDecimal amount,
    Instant createdAt
) {}
