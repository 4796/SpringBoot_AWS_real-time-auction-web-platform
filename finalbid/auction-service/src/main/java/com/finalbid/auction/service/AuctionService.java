package com.finalbid.auction.service;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AuctionService {
    AuctionResponse createAuction(UUID sellerId, AuctionCreateRequest request);
    AuctionResponse getAuction(UUID id);
    Page<AuctionResponse> getActiveAuctions(Pageable pageable);
    Page<AuctionResponse> searchAuctions(AuctionStatus status, Category category, String search, Pageable pageable);
    void closeAuction(UUID auctionId);
}
