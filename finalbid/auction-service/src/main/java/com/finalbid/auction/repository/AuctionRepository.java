package com.finalbid.auction.repository;

import com.finalbid.auction.model.Auction;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuctionRepository extends JpaRepository<Auction, UUID> {

    Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);

    List<Auction> findByStatusAndEndAtBefore(AuctionStatus status, Instant now);

    @Query("SELECT a FROM Auction a WHERE a.status = :status " +
           "AND (CAST(:category AS string) IS NULL OR a.category = :category) " +
           "AND (:search IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Auction> searchAuctions(@Param("status") AuctionStatus status,
                                @Param("category") Category category,
                                @Param("search") String search,
                                Pageable pageable);

    List<Auction> findBySellerId(UUID sellerId);
}
