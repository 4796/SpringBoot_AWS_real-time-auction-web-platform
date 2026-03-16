package com.finalbid.auction.service.impl;

import com.finalbid.auction.dto.BidRequest;
import com.finalbid.auction.dto.BidResponse;
import com.finalbid.auction.messaging.EventPublisher;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.Bid;
import com.finalbid.auction.repository.AuctionRepository;
import com.finalbid.auction.repository.BidRepository;
import com.finalbid.auction.service.BidService;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class BidServiceImpl implements BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final EventPublisher eventPublisher;
    private final List<BidValidationStrategy> validationStrategies;
    private final StringRedisTemplate redisTemplate;

    @Value("${kafka.topic.bid-placed}")
    private String bidPlacedTopic;

    public BidServiceImpl(AuctionRepository auctionRepository,
                          BidRepository bidRepository,
                          EventPublisher eventPublisher,
                          List<BidValidationStrategy> validationStrategies,
                          StringRedisTemplate redisTemplate) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.eventPublisher = eventPublisher;
        this.validationStrategies = validationStrategies;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BidResponse placeBid(UUID bidderId, UUID auctionId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));

        // 1. Run validation pipeline
        validationStrategies.forEach(strategy -> strategy.validate(auction, bidderId, request.amount()));

        // 2. Find previous top bidder BEFORE saving the new bid
        String previousBidderId = bidRepository
            .findTopByAuctionIdOrderByAmountDesc(auctionId)
            .map(b -> b.getBidderId().toString())
            .orElse("");

        // Prevent self-outbid notifications
        if (bidderId.toString().equals(previousBidderId)) {
            previousBidderId = "";
        }

        // 3. Save bid
        Bid bid = new Bid(auction, bidderId, request.amount());
        Bid savedBid = bidRepository.save(bid);

        // 4. Update auction
        auction.setCurrentPrice(request.amount());
        auction.setBidCount(auction.getBidCount() + 1);
        auction.setUpdatedAt(Instant.now());
        auctionRepository.save(auction);

        // 5. Publish Event (Kafka/SNS)
        Map<String, Object> bidPayload = new java.util.HashMap<>();
        bidPayload.put("auctionId", auctionId.toString());
        bidPayload.put("bidderId", bidderId.toString());
        bidPayload.put("bidId", savedBid.getId().toString());
        bidPayload.put("amount", request.amount());
        bidPayload.put("auctionTitle", auction.getTitle());
        bidPayload.put("previousBidderId", previousBidderId);

        eventPublisher.publish(bidPlacedTopic, "BID_PLACED", bidPayload);

        // 5. Broadcast to Redis Pub/Sub for WebSockets
        redisTemplate.convertAndSend("finalbid:ws:bid:" + auctionId, 
            String.format("{\"auctionId\":\"%s\",\"bidderId\":\"%s\",\"amount\":%s}", 
            auctionId, bidderId, request.amount()));

        return mapToResponse(savedBid);
    }

    private BidResponse mapToResponse(Bid b) {
        return new BidResponse(
            b.getId(),
            b.getAuction().getId(),
            b.getBidderId(),
            b.getAmount(),
            b.getCreatedAt()
        );
    }
}
