package com.finalbid.user.service;

import com.finalbid.user.dto.MyProfileResponse;
import com.finalbid.user.dto.ProfilePictureResponse;
import com.finalbid.user.dto.UserProfileResponse;
import com.finalbid.user.exception.UserNotFoundException;
import com.finalbid.user.model.User;
import com.finalbid.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * User profile service implementation.
 * Handles public/private profile retrieval and S3 profile picture uploads.
 * Image validation: content-type + magic bytes (JPEG: FF D8 FF, PNG: 89 50 4E 47).
 * Max file size: 5MB.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final byte[] JPEG_MAGIC   = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
    private static final byte[] PNG_MAGIC    = {(byte)0x89, 0x50, 0x4E, 0x47};

    private final UserRepository userRepository;
    private final S3Client       s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserServiceImpl(UserRepository userRepository, S3Client s3Client) {
        this.userRepository = userRepository;
        this.s3Client       = s3Client;
    }

    // ── Public profile ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return new UserProfileResponse(
            user.getUsername(),
            user.getCreatedAt(),
            0  // Active auction count comes from auction-service; 0 placeholder here
        );
    }

    // ── My profile ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        return new MyProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfilePictureUrl(),
            user.getStatus().name(),
            user.getCreatedAt()
        );
    }

    // ── Profile picture upload ────────────────────────────────────────────────

    @Override
    @Transactional
    public ProfilePictureResponse uploadProfilePicture(UUID userId, MultipartFile file) {
        validateImage(file);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String key = "uploads/profile/" + userId + "/" + UUID.randomUUID() + ".jpg";
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/jpeg")
                .contentLength(file.getSize())
                .build();
            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to upload image: " + e.getMessage());
        }

        // Thumbnail URL is deterministic: replace "uploads/" with "thumbnails/"
        // Lambda will process and populate the actual thumbnail
        String s3BaseUrl = "https://" + bucket + ".s3.amazonaws.com/";
        String imageUrl  = s3BaseUrl + key;

        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);

        return new ProfilePictureResponse(imageUrl);
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No image provided");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Image too large. Maximum size is 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null
                || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Only JPEG and PNG images are allowed");
        }

        // Validate magic bytes
        try {
            byte[] magic = file.getInputStream().readNBytes(4);
            if (!matchesMagic(magic, JPEG_MAGIC) && !matchesMagic(magic, PNG_MAGIC)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File content does not match a valid image format");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read image file");
        }
    }

    private boolean matchesMagic(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) return false;
        }
        return true;
    }
}
