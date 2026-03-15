package com.finalbid.auction.repository;

import com.finalbid.auction.BaseIntegrationTest;
import com.finalbid.auction.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuctionRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    void searchAuctions_WithCasts_Success() {
        // Arrange
        Auction auction = new Auction(
            UUID.randomUUID(),
            "Vintage Nikon Camera",
            "Functional camera",
            Category.ELECTRONICS,
            Condition.USED,
            BigDecimal.valueOf(50.00),
            DurationOption.HOUR_1,
            Instant.now().plusSeconds(3600)
        );
        auctionRepository.save(auction);

        // Act & Assert
        // Test search by category and partial title
        Page<Auction> results = auctionRepository.searchAuctions(
            AuctionStatus.ACTIVE,
            Category.ELECTRONICS,
            "nikon",
            PageRequest.of(0, 10)
        );

        assertFalse(results.isEmpty());
        assertTrue(results.getContent().get(0).getTitle().contains("Nikon"));
        
        // Test with null category
        Page<Auction> allResults = auctionRepository.searchAuctions(
            AuctionStatus.ACTIVE,
            null,
            "nikon",
            PageRequest.of(0, 10)
        );
        assertFalse(allResults.isEmpty());
    }
}
