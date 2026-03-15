package com.finalbid.auction.strategy.impl;

import com.finalbid.auction.exception.BidValidationException;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
@Order(1)
public class AuctionExistenceValidator implements BidValidationStrategy {
    @Override
    public void validate(Auction auction, UUID bidderId, BigDecimal amount) {
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BidValidationException("Auction is not active or does not exist", "AUCTION_NOT_ACTIVE");
        }
    }
}
