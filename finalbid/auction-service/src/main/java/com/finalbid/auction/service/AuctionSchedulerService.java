package com.finalbid.auction.service;

import com.finalbid.auction.messaging.EventPublisher;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.repository.AuctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuctionSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionSchedulerService.class);

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;
    private final EventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    @Value("${kafka.topic.auction-ended}")
    private String auctionEndedTopic;

    public AuctionSchedulerService(AuctionRepository auctionRepository,
                                   AuctionService auctionService,
                                   EventPublisher eventPublisher,
                                   StringRedisTemplate redisTemplate) {
        this.auctionRepository = auctionRepository;
        this.auctionService = auctionService;
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void closeExpiredAuctions() {
        logger.info("Checking for expired auctions...");
        Instant now = Instant.now();
        List<Auction> expired = auctionRepository.findByStatusAndEndAtBefore(AuctionStatus.ACTIVE, now);

        for (Auction auction : expired) {
            try {
                logger.info("Closing auction: {}", auction.getId());
                auctionService.closeAuction(auction.getId());

                // Publish Event
                eventPublisher.publish(auctionEndedTopic, "AUCTION_ENDED", Map.of(
                    "auctionId", auction.getId(),
                    "winnerId", auction.getWinnerId() != null ? auction.getWinnerId() : "NONE",
                    "finalPrice", auction.getCurrentPrice()
                ));

                // Broadcast WebSocket message
                redisTemplate.convertAndSend("finalbid:ws:ended:" + auction.getId(), 
                    String.format("{\"auctionId\":\"%s\",\"winnerId\":\"%s\",\"finalPrice\":%s}", 
                    auction.getId(), 
                    auction.getWinnerId() != null ? auction.getWinnerId() : "NONE", 
                    auction.getCurrentPrice()));

            } catch (Exception e) {
                logger.error("Failed to close auction: " + auction.getId(), e);
            }
        }
    }
}
