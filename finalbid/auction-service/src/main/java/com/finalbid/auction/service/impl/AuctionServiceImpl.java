package com.finalbid.auction.service.impl;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.model.DurationOption;
import com.finalbid.auction.repository.AuctionRepository;
import com.finalbid.auction.service.AuctionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.finalbid.auction.repository.BidRepository;

@Service
@Transactional
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public AuctionServiceImpl(AuctionRepository auctionRepository, BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    @Override
    public AuctionResponse createAuction(UUID sellerId, AuctionCreateRequest request) {
        Instant endAt = calculateEndAt(request.durationOption());
        
        Auction auction = new Auction(
            sellerId,
            request.title(),
            request.description(),
            request.category(),
            request.condition(),
            request.startPrice(),
            request.durationOption(),
            endAt
        );
        
        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionResponse getAuction(UUID id) {
        Auction auction = auctionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        return mapToResponse(auction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getActiveAuctions(Pageable pageable) {
        return auctionRepository.findByStatus(AuctionStatus.ACTIVE, pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> searchAuctions(AuctionStatus status, Category category, String search, Pageable pageable) {
        return auctionRepository.searchAuctions(status, category, search, pageable)
            .map(this::mapToResponse);
    }

    @Override
    public void closeAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            auction.setStatus(AuctionStatus.ENDED);
            
            bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)
                .ifPresent(topBid -> auction.setWinnerId(topBid.getBidderId()));

            auction.setUpdatedAt(Instant.now());
            auctionRepository.save(auction);
        }
    }

    private Instant calculateEndAt(DurationOption option) {
        Duration duration = switch (option) {
            case MIN_1 -> Duration.ofMinutes(1);
            case MIN_5 -> Duration.ofMinutes(5);
            case MIN_10 -> Duration.ofMinutes(10);
            case MIN_30 -> Duration.ofMinutes(30);
            case HOUR_1 -> Duration.ofHours(1);
            case HOUR_2 -> Duration.ofHours(2);
        };
        return Instant.now().plus(duration);
    }

    private AuctionResponse mapToResponse(Auction a) {
        return new AuctionResponse(
            a.getId(),
            a.getSellerId(),
            a.getTitle(),
            a.getDescription(),
            a.getCategory(),
            a.getCondition(),
            a.getStartPrice(),
            a.getCurrentPrice(),
            a.getBidCount(),
            a.getEndAt(),
            a.getStatus(),
            a.getImageUrl(),
            a.getWinnerId(),
            a.getCreatedAt()
        );
    }
}
