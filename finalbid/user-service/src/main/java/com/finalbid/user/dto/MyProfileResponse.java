package com.finalbid.user.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for GET /api/users/me - full profile with bid history.
 */
public record MyProfileResponse(
    UUID userId,
    String username,
    String email,
    String profilePictureUrl,
    String status,
    Instant memberSince
) {}
