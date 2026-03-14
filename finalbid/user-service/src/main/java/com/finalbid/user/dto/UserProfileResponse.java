package com.finalbid.user.dto;

import java.time.Instant;

/**
 * Public profile response for GET /api/users/{username}.
 */
public record UserProfileResponse(
    String username,
    Instant memberSince,
    int activeAuctionCount
) {}
