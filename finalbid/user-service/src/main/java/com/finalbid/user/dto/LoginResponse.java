package com.finalbid.user.dto;

import java.util.UUID;

/**
 * Response body for POST /api/auth/login.
 */
public record LoginResponse(
    String accessToken,
    String username,
    UUID   userId,
    boolean emailVerified
) {}
