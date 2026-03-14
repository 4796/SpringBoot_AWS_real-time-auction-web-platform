package com.finalbid.user.service;

import com.finalbid.user.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT creation and validation for both access and refresh tokens.
 *
 * Access token:
 *   - HS256
 *   - Expiry: 15 minutes
 *   - Claims: sub (userId), username, email, status, jti (UUID v4)
 *
 * Refresh token:
 *   - Opaque UUID v4 token stored as BCrypt hash in DB
 *   - Also carries a jti (UUID v4) for Redis blacklisting
 *   - Expiry: 7 days
 *   - Issued as a signed JWT (HttpOnly cookie) so we can validate without DB hit
 */
@Service
public class JwtService {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_EMAIL    = "email";
    private static final String CLAIM_STATUS   = "status";

    private final SecretKey            signingKey;
    private final long                 accessExpiryMs;
    private final long                 refreshExpiryDays;
    private final StringRedisTemplate  redisTemplate;

    public JwtService(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-expiry-ms}") long accessExpiryMs,
            @Value("${jwt.refresh-expiry-days}") long refreshExpiryDays,
            StringRedisTemplate redisTemplate) {
        this.signingKey        = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessExpiryMs    = accessExpiryMs;
        this.refreshExpiryDays = refreshExpiryDays;
        this.redisTemplate     = redisTemplate;
    }

    // ── Access Token ──────────────────────────────────────────────────────────

    /**
     * Issues a new access token for the given user.
     * Contains: sub=userId, username, email, status, jti, iat, exp.
     */
    public String issueAccessToken(User user) {
        Instant now    = Instant.now();
        Instant expiry = now.plusMillis(accessExpiryMs);
        return Jwts.builder()
                   .subject(user.getId().toString())
                   .claim(CLAIM_USERNAME, user.getUsername())
                   .claim(CLAIM_EMAIL,    user.getEmail())
                   .claim(CLAIM_STATUS,   user.getStatus().name())
                   .id(UUID.randomUUID().toString())          // jti
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(expiry))
                   .signWith(signingKey)
                   .compact();
    }

    /**
     * Issues a new refresh token (signed JWT).
     * Contains: sub=userId, jti for blacklisting.
     */
    public String issueRefreshToken(User user) {
        Instant now    = Instant.now();
        Instant expiry = now.plus(Duration.ofDays(refreshExpiryDays));
        return Jwts.builder()
                   .subject(user.getId().toString())
                   .id(UUID.randomUUID().toString())          // jti
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(expiry))
                   .signWith(signingKey)
                   .compact();
    }

    // ── Parsing & Validation ──────────────────────────────────────────────────

    /**
     * Parses and validates a JWT. Throws on invalid/expired token.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                   .verifyWith(signingKey)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    public String extractStatus(String token) {
        return parseClaims(token).get(CLAIM_STATUS, String.class);
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public Instant extractExpiry(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /**
     * Returns true if the token signature and expiry are valid,
     * AND the jti is NOT in the Redis blacklist.
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            String jti    = claims.getId();
            // Check blacklist
            String blacklistKey = "blacklist:refresh:" + jti;
            Boolean blacklisted = redisTemplate.hasKey(blacklistKey);
            return blacklisted == null || !blacklisted;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Redis Blacklist ───────────────────────────────────────────────────────

    /**
     * Adds a refresh token's jti to the Redis blacklist with TTL equal to
     * the token's remaining lifetime. Called on logout.
     */
    public void blacklistRefreshToken(String refreshToken) {
        try {
            Claims  claims    = parseClaims(refreshToken);
            String  jti       = claims.getId();
            Instant expiry    = claims.getExpiration().toInstant();
            long    ttlSeconds = Duration.between(Instant.now(), expiry).toSeconds();
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(
                    "blacklist:refresh:" + jti,
                    "1",
                    Duration.ofSeconds(ttlSeconds)
                );
            }
        } catch (JwtException e) {
            // Token already invalid; no need to blacklist
        }
    }

    /**
     * Checks whether the jti from a refresh token is in the Redis blacklist.
     * Must be called BEFORE signature validation to stop stolen token reuse.
     */
    public boolean isRefreshTokenBlacklisted(String jti) {
        Boolean blacklisted = redisTemplate.hasKey("blacklist:refresh:" + jti);
        return Boolean.TRUE.equals(blacklisted);
    }

    public long getRefreshExpiryDays() {
        return refreshExpiryDays;
    }
}
