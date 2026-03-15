package com.finalbid.auction.controller;

import com.finalbid.auction.dto.BidRequest;
import com.finalbid.auction.dto.BidResponse;
import com.finalbid.auction.service.BidService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID auctionId,
            @Valid @RequestBody BidRequest request) {
        return ResponseEntity.status(201).body(bidService.placeBid(UUID.fromString(userId), auctionId, request));
    }
}
