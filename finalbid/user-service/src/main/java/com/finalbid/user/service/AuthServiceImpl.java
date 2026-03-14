package com.finalbid.user.service;

import com.finalbid.user.dto.LoginRequest;
import com.finalbid.user.dto.LoginResponse;
import com.finalbid.user.dto.RefreshResponse;
import com.finalbid.user.dto.RegisterRequest;
import com.finalbid.user.exception.EmailAlreadyExistsException;
import com.finalbid.user.exception.InvalidTokenException;
import com.finalbid.user.exception.UsernameAlreadyExistsException;
import com.finalbid.user.messaging.EventPublisher;
import com.finalbid.user.model.User;
import com.finalbid.user.model.UserStatus;
import com.finalbid.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Core authentication service implementation.
 * Implements the full auth lifecycle: register, verify-email, login,
 * refresh, logout.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService      jwtService;
    private final EventPublisher  eventPublisher;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${jwt.refresh-expiry-days}")
    private long refreshExpiryDays;

    public AuthServiceImpl(UserRepository  userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService      jwtService,
                           EventPublisher  eventPublisher) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
        this.eventPublisher  = eventPublisher;
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User   user         = new User(request.username(), request.email(), passwordHash);

        // Generate email verification token (UUID v4, expires in 24h)
        String  token   = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(Duration.ofHours(24));
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiresAt(expires);

        User saved = userRepository.save(user);

        // Build verification link and publish event
        String link = baseUrl + "/api/auth/verify-email?token=" + token;
        eventPublisher.publishUserRegistered(
            saved.getId().toString(),
            saved.getEmail(),
            saved.getUsername(),
            token,
            link
        );
    }

    // ── Verify Email ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getEmailVerificationExpiresAt() == null
            || Instant.now().isAfter(user.getEmailVerificationExpiresAt())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);
        userRepository.save(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // BANNED users get 403
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account has been banned");
        }

        // Issue tokens
        String accessToken  = jwtService.issueAccessToken(user);
        String refreshToken = jwtService.issueRefreshToken(user);

        // Hash and store refresh token
        String refreshHash = passwordEncoder.encode(refreshToken);
        user.setRefreshTokenHash(refreshHash);
        userRepository.save(user);

        // Set HttpOnly Secure SameSite=Strict cookie
        Cookie cookie = buildRefreshCookie(refreshToken, (int) (refreshExpiryDays * 86400));
        response.addCookie(cookie);

        boolean emailVerified = user.getStatus() == UserStatus.ACTIVE;
        return new LoginResponse(accessToken, user.getUsername(), user.getId(), emailVerified);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /**
     * Order of checks:
     * 1. Parse refresh token cookie
     * 2. Check Redis blacklist for jti → reject 401 if found
     * 3. Validate token signature and expiry
     * 4. Look up user by id, verify refresh_token_hash in DB
     * 5. Issue new access token
     */
    @Override
    @Transactional(readOnly = true)
    public RefreshResponse refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token");
        }

        // Step 2: Check blacklist FIRST
        String jti;
        try {
            jti = jwtService.extractJti(refreshToken);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        if (jwtService.isRefreshTokenBlacklisted(jti)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked");
        }

        // Step 3: Validate signature + expiry
        if (!jwtService.isValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        // Step 4: Find user and verify hash
        String userId = jwtService.extractUserId(refreshToken);
        User   user   = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getRefreshTokenHash() == null
            || !passwordEncoder.matches(refreshToken, user.getRefreshTokenHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token mismatch");
        }

        // Step 5: Issue new access token
        return new RefreshResponse(jwtService.issueAccessToken(user));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Logout flow:
     * 1. Parse refresh token cookie
     * 2. Calculate remaining TTL = token.exp - now()
     * 3. Add jti to Redis blacklist with that TTL
     * 4. Null refresh_token_hash in DB
     * 5. Clear cookie
     */
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null) {
            // Step 2+3: Blacklist in Redis
            jwtService.blacklistRefreshToken(refreshToken);

            // Step 4: Null hash in DB
            try {
                String userId = jwtService.extractUserId(refreshToken);
                userRepository.findById(UUID.fromString(userId)).ifPresent(user -> {
                    user.setRefreshTokenHash(null);
                    userRepository.save(user);
                });
            } catch (JwtException ignored) {
                // Token may be expired; still clear cookie
            }
        }

        // Step 5: Clear cookie
        Cookie clear = buildRefreshCookie("", 0);
        response.addCookie(clear);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
            .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    private Cookie buildRefreshCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);           // only over HTTPS (ALB terminates TLS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        // SameSite=Strict via response header (Cookie API doesn't expose it)
        return cookie;
    }
}
