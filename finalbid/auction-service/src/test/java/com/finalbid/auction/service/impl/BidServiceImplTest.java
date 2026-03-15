package com.finalbid.auction.service.impl;

import com.finalbid.auction.dto.BidRequest;
import com.finalbid.auction.dto.BidResponse;
import com.finalbid.auction.messaging.EventPublisher;
import com.finalbid.auction.model.*;
import com.finalbid.auction.repository.AuctionRepository;
import com.finalbid.auction.repository.BidRepository;
import com.finalbid.auction.strategy.BidValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private BidValidationStrategy validationStrategy;

    private BidServiceImpl bidService;

    private UUID bidderId;
    private UUID auctionId;
    private Auction auction;

    @BeforeEach
    void setUp() {
        bidService = new BidServiceImpl(
            auctionRepository,
            bidRepository,
            eventPublisher,
            Collections.singletonList(validationStrategy),
            redisTemplate
        );
        ReflectionTestUtils.setField(bidService, "bidPlacedTopic", "test-topic");

        bidderId = UUID.randomUUID();
        auctionId = UUID.randomUUID();
        auction = new Auction(
            UUID.randomUUID(),
            "Title",
            "Desc",
            Category.ELECTRONICS,
            Condition.NEW,
            BigDecimal.valueOf(100.00),
            DurationOption.HOUR_1,
            Instant.now().plusSeconds(3600)
        );
    }

    @Test
    void placeBid_Success() {
        // Arrange
        BidRequest request = new BidRequest(BigDecimal.valueOf(110.00));
        Bid bid = new Bid(auction, bidderId, request.amount());
        ReflectionTestUtils.setField(bid, "id", UUID.randomUUID());
        
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        // Act
        BidResponse response = bidService.placeBid(bidderId, auctionId, request);

        // Assert
        assertNotNull(response);
        assertEquals(request.amount(), response.amount());
        verify(validationStrategy, times(1)).validate(eq(auction), eq(bidderId), eq(request.amount()));
        verify(bidRepository, times(1)).save(any(Bid.class));
        verify(auctionRepository, times(1)).save(auction);
        verify(eventPublisher, times(1)).publish(anyString(), anyString(), any());
        verify(redisTemplate, times(1)).convertAndSend(anyString(), anyString());
    }

    @Test
    void placeBid_AuctionNotFound_ThrowsException() {
        // Arrange
        BidRequest request = new BidRequest(BigDecimal.valueOf(110.00));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bidService.placeBid(bidderId, auctionId, request));
        verify(bidRepository, never()).save(any());
    }
}
