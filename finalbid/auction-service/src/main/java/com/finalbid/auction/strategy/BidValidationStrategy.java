package com.finalbid.auction.strategy;

import com.finalbid.auction.model.Auction;
import java.math.BigDecimal;
import java.util.UUID;

public interface BidValidationStrategy {
    /**
     * Validates a bid attempt for a specific auction.
     * Throws BidValidationException if validation fails.
     */
    void validate(Auction auction, UUID bidderId, BigDecimal amount);
}
