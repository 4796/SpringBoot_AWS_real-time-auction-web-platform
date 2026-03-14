package com.finalbid.user.service;

import com.finalbid.user.dto.MyProfileResponse;
import com.finalbid.user.dto.ProfilePictureResponse;
import com.finalbid.user.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * User profile service interface.
 */
public interface UserService {

    /** GET /api/users/{username} — public profile */
    UserProfileResponse getPublicProfile(String username);

    /** GET /api/users/me — full authenticated user profile */
    MyProfileResponse getMyProfile(UUID userId);

    /**
     * PUT /api/users/me/profile-picture
     * Validates image content-type + magic bytes, uploads to S3.
     */
    ProfilePictureResponse uploadProfilePicture(UUID userId, MultipartFile file);
}
