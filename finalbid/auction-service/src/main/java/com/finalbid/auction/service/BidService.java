package com.finalbid.auction.service;

import com.finalbid.auction.dto.BidRequest;
import com.finalbid.auction.dto.BidResponse;
import java.util.UUID;

public interface BidService {
    BidResponse placeBid(UUID bidderId, UUID auctionId, BidRequest request);
}
