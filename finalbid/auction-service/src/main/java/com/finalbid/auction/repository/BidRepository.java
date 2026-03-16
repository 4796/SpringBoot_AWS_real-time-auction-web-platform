package com.finalbid.auction.repository;

import com.finalbid.auction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    List<Bid> findByBidderIdOrderByCreatedAtDesc(UUID bidderId);

    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(UUID auctionId);
}
