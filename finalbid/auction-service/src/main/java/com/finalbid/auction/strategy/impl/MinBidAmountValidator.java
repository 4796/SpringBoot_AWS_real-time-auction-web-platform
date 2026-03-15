package com.finalbid.auction.strategy.impl;

import com.finalbid.auction.exception.BidValidationException;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
@Order(4)
public class MinBidAmountValidator implements BidValidationStrategy {
    @Override
    public void validate(Auction auction, UUID bidderId, BigDecimal amount) {
        if (auction.getBidCount() == 0) {
            if (amount.compareTo(auction.getStartPrice()) < 0) {
                throw new BidValidationException("Bid must be at least the starting price", "BID_BELOW_START_PRICE");
            }
        }
    }
}
