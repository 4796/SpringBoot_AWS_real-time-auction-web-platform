package com.finalbid.auction.service.decorator;

import com.finalbid.auction.dto.BidRequest;
import com.finalbid.auction.dto.BidResponse;
import com.finalbid.auction.service.BidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Primary
public class LoggingBidServiceDecorator implements BidService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBidServiceDecorator.class);
    private final BidService delegate;

    public LoggingBidServiceDecorator(BidService delegate) {
        this.delegate = delegate;
    }

    @Override
    public BidResponse placeBid(UUID bidderId, UUID auctionId, BidRequest request) {
        logger.info("ATTEMPT placeBid: bidderId={}, auctionId={}, amount={}", bidderId, auctionId, request.amount());
        try {
            BidResponse response = delegate.placeBid(bidderId, auctionId, request);
            logger.info("SUCCESS placeBid: bidId={}", response.id());
            return response;
        } catch (Exception e) {
            logger.error("ERROR placeBid: bidderId={}, auctionId={}, error={}", bidderId, auctionId, e.getMessage());
            throw e;
        }
    }
}
