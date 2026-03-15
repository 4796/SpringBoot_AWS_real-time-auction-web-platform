package com.finalbid.auction.strategy.impl;

import com.finalbid.auction.exception.BidValidationException;
import com.finalbid.auction.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BidValidationStrategiesTest {

    private UUID sellerId;
    private UUID bidderId;
    private Auction auction;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        bidderId = UUID.randomUUID();
        auction = new Auction(
            sellerId,
            "Test",
            "Desc",
            Category.ELECTRONICS,
            Condition.NEW,
            BigDecimal.valueOf(100.00),
            DurationOption.HOUR_1,
            Instant.now().plusSeconds(3600)
        );
    }

    @Test
    void auctionBidderNotSellerValidator_SameUser_ThrowsException() {
        AuctionBidderNotSellerValidator validator = new AuctionBidderNotSellerValidator();
        assertThrows(BidValidationException.class, () -> 
            validator.validate(auction, sellerId, BigDecimal.valueOf(110.00)));
    }

    @Test
    void auctionExpiryValidator_Expired_ThrowsException() {
        AuctionExpiryValidator validator = new AuctionExpiryValidator();
        auction.setEndAt(Instant.now().minusSeconds(1));
        assertThrows(BidValidationException.class, () -> 
            validator.validate(auction, bidderId, BigDecimal.valueOf(110.00)));
    }

    @Test
    void minBidAmountValidator_LowerThanCurrent_ThrowsException() {
        MinBidAmountValidator validator = new MinBidAmountValidator();
        assertThrows(BidValidationException.class, () -> 
            validator.validate(auction, bidderId, BigDecimal.valueOf(90.00)));
    }

    @Test
    void bidIncrementValidator_TooSmall_ThrowsException() {
        BidIncrementValidator validator = new BidIncrementValidator();
        auction.setBidCount(1);
        // If current is 100, increment is max(1, 1) = 1. Min required = 101.
        assertThrows(BidValidationException.class, () -> 
            validator.validate(auction, bidderId, BigDecimal.valueOf(100.10)));
    }
}
