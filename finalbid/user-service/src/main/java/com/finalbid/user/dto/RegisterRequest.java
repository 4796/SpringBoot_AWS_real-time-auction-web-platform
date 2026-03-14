package com.finalbid.user.dto;

import jakarta.validation.constraints.*;

/**
 * Request body for POST /api/auth/register.
 * Validation rules from spec Section 6.1.
 */
public record RegisterRequest(

    @NotBlank
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Username may only contain letters, digits, and underscores")
    String username,

    @NotBlank
    @Email(message = "Must be a valid email address")
    String email,

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
) {}
