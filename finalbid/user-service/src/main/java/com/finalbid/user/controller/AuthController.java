package com.finalbid.user.controller;

import com.finalbid.user.dto.*;
import com.finalbid.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * REST controller for all /api/auth/* endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Auth: none
     * Response: 201 + message
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Check your email to verify your account"));
    }

    /**
     * GET /api/auth/verify-email?token={token}
     * Auth: none
     * On success: redirect to /login?verified=true
     * On failure: redirect to /login?error=invalid_token
     */
    @GetMapping("/verify-email")
    public RedirectView verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return new RedirectView("/login?verified=true");
        } catch (Exception e) {
            return new RedirectView("/login?error=invalid_token");
        }
    }

    /**
     * POST /api/auth/login
     * Auth: none
     * Response: 200 + LoginResponse + HttpOnly refreshToken cookie
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * POST /api/auth/refresh
     * Auth: refresh token cookie
     * Response: 200 + new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * POST /api/auth/logout
     * Auth: Bearer JWT
     * Response: 204
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest  request,
                                       HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
