package com.finalbid.auction.service.decorator;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.service.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Primary
public class LoggingAuctionServiceDecorator implements AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAuctionServiceDecorator.class);
    private final AuctionService delegate;

    public LoggingAuctionServiceDecorator(AuctionService delegate) {
        this.delegate = delegate;
    }

    @Override
    public AuctionResponse createAuction(UUID sellerId, AuctionCreateRequest request) {
        logger.info("ATTEMPT createAuction: sellerId={}, title={}", sellerId, request.title());
        try {
            AuctionResponse response = delegate.createAuction(sellerId, request);
            logger.info("SUCCESS createAuction: auctionId={}", response.id());
            return response;
        } catch (Exception e) {
            logger.error("ERROR createAuction: sellerId={}, error={}", sellerId, e.getMessage());
            throw e;
        }
    }

    @Override
    public AuctionResponse getAuction(UUID id) {
        return delegate.getAuction(id);
    }

    @Override
    public Page<AuctionResponse> getActiveAuctions(Pageable pageable) {
        return delegate.getActiveAuctions(pageable);
    }

    @Override
    public Page<AuctionResponse> searchAuctions(AuctionStatus status, Category category, String search, Pageable pageable) {
        return delegate.searchAuctions(status, category, search, pageable);
    }

    @Override
    public void closeAuction(UUID auctionId) {
        logger.info("ATTEMPT closeAuction: id={}", auctionId);
        try {
            delegate.closeAuction(auctionId);
            logger.info("SUCCESS closeAuction: id={}", auctionId);
        } catch (Exception e) {
            logger.error("ERROR closeAuction: id={}, error={}", auctionId, e.getMessage());
            throw e;
        }
    }
}
