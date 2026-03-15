package com.finalbid.auction.controller;

import com.finalbid.auction.dto.AuctionCreateRequest;
import com.finalbid.auction.dto.AuctionResponse;
import com.finalbid.auction.model.AuctionStatus;
import com.finalbid.auction.model.Category;
import com.finalbid.auction.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AuctionCreateRequest request) {
        return ResponseEntity.status(201).body(auctionService.createAuction(UUID.fromString(userId), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable UUID id) {
        return ResponseEntity.ok(auctionService.getAuction(id));
    }

    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> search(
            @RequestParam(defaultValue = "ACTIVE") AuctionStatus status,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(auctionService.searchAuctions(status, category, search, PageRequest.of(page, size)));
    }
}
