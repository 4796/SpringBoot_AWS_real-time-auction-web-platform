package com.finalbid.user.controller;

import com.finalbid.user.dto.MyProfileResponse;
import com.finalbid.user.dto.ProfilePictureResponse;
import com.finalbid.user.dto.UserProfileResponse;
import com.finalbid.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * User profile endpoints.
 *
 * IMPORTANT: user ID is ALWAYS read from the JWT (Authentication principal),
 * never from the request body or path variable for self-actions.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Auth: Bearer JWT
     */
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getMyProfile(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName()); // auth.getName() = userId from JWT
        return ResponseEntity.ok(userService.getMyProfile(userId));
    }

    /**
     * GET /api/users/{username}
     * Auth: Bearer JWT (ACTIVE users only — enforced by caller / display logic)
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getPublicProfile(
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getPublicProfile(username));
    }

    /**
     * PUT /api/users/me/profile-picture
     * Auth: Bearer JWT (ACTIVE users only)
     * Body: multipart/form-data, field name "image"
     */
    @PutMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfilePictureResponse> uploadProfilePicture(
            @RequestPart("image") MultipartFile image,
            Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(userService.uploadProfilePicture(userId, image));
    }

    /**
     * GET /api/users/me/auctions
     * Auth: Bearer JWT
     * NOTE: Auction data lives in auction-service. This endpoint in user-service
     * returns the userId for the frontend to query auction-service directly,
     * or returns an empty page here (auction-service holds this data).
     * Returning the userId for cross-service routing.
     */
    @GetMapping("/me/auctions")
    public ResponseEntity<?> getMyAuctions(Authentication auth) {
        // Auction history is in auction-service; return userId for routing
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("userId", auth.getName());
            put("message", "Query auction-service with this userId");
        }});
    }

    /**
     * GET /api/users/me/bids
     * Auth: Bearer JWT
     * NOTE: Bid data lives in auction-service. Same as above.
     */
    @GetMapping("/me/bids")
    public ResponseEntity<?> getMyBids(Authentication auth) {
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("userId", auth.getName());
            put("message", "Query auction-service with this userId");
        }});
    }
}
