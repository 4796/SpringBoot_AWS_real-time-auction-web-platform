package com.finalbid.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/login.
 */
public record LoginRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password
) {}
