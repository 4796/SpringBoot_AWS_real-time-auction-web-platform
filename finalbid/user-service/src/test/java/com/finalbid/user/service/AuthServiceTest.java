package com.finalbid.user.service;

import com.finalbid.user.BaseIntegrationTest;
import com.finalbid.user.dto.RegisterRequest;
import com.finalbid.user.exception.EmailAlreadyExistsException;
import com.finalbid.user.model.User;
import com.finalbid.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for AuthServiceImpl.
 */
class AuthServiceTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@finalbid.com", "Password123");

        // Act
        authService.register(request);

        // Assert
        Optional<User> userOptional = userRepository.findByEmail("test@finalbid.com");
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getUsername()).isEqualTo("testuser");
        assertThat(userOptional.get().getEmailVerificationToken()).isNotNull();
    }

    @Test
    void shouldThrowWhenEmailExists() {
        // Arrange
        RegisterRequest request1 = new RegisterRequest("testuser1", "duplicate@finalbid.com", "Password123");
        authService.register(request1);

        RegisterRequest request2 = new RegisterRequest("testuser2", "duplicate@finalbid.com", "Password123");

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request2));
    }

    @Test
    void shouldThrowWhenPendingVerificationLogin() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("pendinguser", "pending@finalbid.com", "Password123");
        authService.register(registerRequest);

        com.finalbid.user.dto.LoginRequest loginRequest = new com.finalbid.user.dto.LoginRequest("pending@finalbid.com", "Password123");
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception = assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () -> authService.login(loginRequest, response)
        );
        assertThat(exception.getStatusCode().value()).isEqualTo(403);
        assertThat(exception.getReason()).isEqualTo("Email not verified");
    }
}
