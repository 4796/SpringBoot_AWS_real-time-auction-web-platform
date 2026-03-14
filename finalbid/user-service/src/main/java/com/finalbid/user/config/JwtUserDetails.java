package com.finalbid.user.config;

/**
 * Additional JWT-derived details attached to the authentication object.
 * Available via ((JwtUserDetails) auth.getDetails()).
 */
public record JwtUserDetails(String userId, String username, String status) {}
