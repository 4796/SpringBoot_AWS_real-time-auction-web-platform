package com.finalbid.user.service;

import com.finalbid.user.dto.LoginRequest;
import com.finalbid.user.dto.LoginResponse;
import com.finalbid.user.dto.RefreshResponse;
import com.finalbid.user.dto.RegisterRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Auth service interface. Implementations provide the full authentication lifecycle.
 */
public interface AuthService {

    /**
     * Register a new user. Publishes USER_REGISTERED event for email verification.
     */
    void register(RegisterRequest request);

    /**
     * Verify email using the token from the verification link.
     * Returns "success" or throws InvalidTokenException / ExpiredTokenException.
     */
    void verifyEmail(String token);

    /**
     * Login with email/password. Returns access token + sets refresh token HttpOnly cookie.
     */
    LoginResponse login(LoginRequest request, HttpServletResponse response);

    /**
     * Refresh: check blacklist → validate token → verify DB hash → issue new access token.
     */
    RefreshResponse refresh(HttpServletRequest request);

    /**
     * Logout: blacklist jti in Redis, null hash in DB, clear cookie.
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
