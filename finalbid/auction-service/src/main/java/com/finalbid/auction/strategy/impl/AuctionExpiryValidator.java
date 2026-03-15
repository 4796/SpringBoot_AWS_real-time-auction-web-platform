package com.finalbid.auction.strategy.impl;

import com.finalbid.auction.exception.BidValidationException;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@Order(2)
public class AuctionExpiryValidator implements BidValidationStrategy {
    @Override
    public void validate(Auction auction, UUID bidderId, BigDecimal amount) {
        if (auction.getEndAt().isBefore(Instant.now())) {
            throw new BidValidationException("Auction has already expired", "AUCTION_EXPIRED");
        }
    }
}
