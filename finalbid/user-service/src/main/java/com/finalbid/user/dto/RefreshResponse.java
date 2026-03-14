package com.finalbid.user.dto;

/**
 * Response body for POST /api/auth/refresh.
 */
public record RefreshResponse(String accessToken) {}
