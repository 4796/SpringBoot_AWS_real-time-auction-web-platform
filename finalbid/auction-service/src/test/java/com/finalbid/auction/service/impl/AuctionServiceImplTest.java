package com.finalbid.auction.service.impl;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.model.Condition;
import com.finalbid.auction.model.DurationOption;
import com.finalbid.auction.repository.AuctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private UUID sellerId;
    private AuctionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        createRequest = new AuctionCreateRequest(
            "Test Auction",
            "This is a test description.",
            Category.ELECTRONICS,
            Condition.NEW,
            BigDecimal.valueOf(100.00),
            DurationOption.HOUR_1
        );
    }

    @Test
    void createAuction_Success() {
        // Arrange
        Auction savedAuction = new Auction(
            sellerId,
            createRequest.title(),
            createRequest.description(),
            createRequest.category(),
            createRequest.condition(),
            createRequest.startPrice(),
            createRequest.durationOption(),
            Instant.now().plusSeconds(3600)
        );
        // Using reflection or a package-private setter if id is not public, but let's assume we can set it via Mockito or just use the object
        when(auctionRepository.save(any(Auction.class))).thenReturn(savedAuction);

        // Act
        AuctionResponse response = auctionService.createAuction(sellerId, createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(createRequest.title(), response.title());
        assertEquals(createRequest.startPrice(), response.startPrice());
        verify(auctionRepository, times(1)).save(any(Auction.class));
    }

    @Test
    void getAuction_Success() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        Auction auction = new Auction(
            sellerId,
            "Title",
            "Desc",
            Category.ELECTRONICS,
            Condition.NEW,
            BigDecimal.TEN,
            DurationOption.MIN_5,
            Instant.now().plusSeconds(300)
        );
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        // Act
        AuctionResponse response = auctionService.getAuction(auctionId);

        // Assert
        assertNotNull(response);
        assertEquals("Title", response.title());
    }

    @Test
    void getAuction_NotFound_ThrowsException() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> auctionService.getAuction(auctionId));
    }
}
