package com.finalbid.auction.strategy.impl;

import com.finalbid.auction.exception.BidValidationException;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@Order(5)
public class BidIncrementValidator implements BidValidationStrategy {
    @Override
    public void validate(Auction auction, UUID bidderId, BigDecimal amount) {
        if (auction.getBidCount() > 0) {
            BigDecimal currentPrice = auction.getCurrentPrice();
            // Increment = max(1% of currentPrice, $1.00)
            BigDecimal percentageIncrement = currentPrice.multiply(BigDecimal.valueOf(0.01));
            BigDecimal minIncrement = percentageIncrement.max(BigDecimal.ONE);
            
            BigDecimal requiredMin = currentPrice.add(minIncrement).setScale(2, RoundingMode.HALF_UP);
            
            if (amount.compareTo(requiredMin) < 0) {
                throw new BidValidationException(
                    "Bid must be at least " + requiredMin + " (current price " + currentPrice + " + minimum increment)",
                    "BID_BELOW_MINIMUM_INCREMENT"
                );
            }
        }
    }
}
