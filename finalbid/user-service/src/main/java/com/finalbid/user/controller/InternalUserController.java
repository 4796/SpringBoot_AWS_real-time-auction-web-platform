package com.finalbid.user.controller;

import com.finalbid.user.dto.InternalUserResponse;
import com.finalbid.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal endpoint for cross-service communication.
 * Not exposed publicly — accessible only within the Docker network.
 * No JWT required (permitted via SecurityConfig).
 */
@RestController
@RequestMapping("/internal")
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /internal/users/{id}
     * Used by notification-service to look up a user's email and username by UUID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<InternalUserResponse> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(new InternalUserResponse(user.getEmail(), user.getUsername())))
            .orElse(ResponseEntity.notFound().build());
    }
}
