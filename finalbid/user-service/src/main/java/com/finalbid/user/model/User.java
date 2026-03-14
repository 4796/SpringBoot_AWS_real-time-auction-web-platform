package com.finalbid.user.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * JPA entity mapping to user_schema.users table.
 *
 * NOTE: username and email are immutable after creation (no setters).
 * password_hash stores BCrypt strength-12 hash only.
 */
@Entity
@Table(
    name    = "users",
    schema  = "user_schema",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_users_email",    columnNames = "email")
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Immutable. Set once on registration.
     * Validates: 3-50 chars alphanumeric + underscore.
     */
    @Column(name = "username", length = 50, nullable = false, updatable = false)
    private String username;

    /**
     * Immutable. Set once on registration.
     */
    @Column(name = "email", length = 255, nullable = false, updatable = false)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "user_schema.user_status")
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "role", nullable = false, columnDefinition = "user_schema.user_role")
    private UserRole role = UserRole.USER;

    /** S3 thumbnail URL after Lambda processing. */
    @Column(name = "profile_picture_url", length = 512)
    private String profilePictureUrl;

    /** UUID token for email verification. Nulled after use. */
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    /** Token is valid until this timestamp. */
    @Column(name = "email_verification_expires_at")
    private Instant emailVerificationExpiresAt;

    /** BCrypt hash of the current refresh token. Null when logged out. */
    @Column(name = "refresh_token_hash", length = 255)
    private String refreshTokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /** Updated via DB trigger on every UPDATE. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    // ── Constructors ──────────────────────────────────────────────────────────

    protected User() {}

    public User(String username, String email, String passwordHash) {
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.status       = UserStatus.PENDING_VERIFICATION;
        this.role         = UserRole.USER;
        this.createdAt    = Instant.now();
        this.updatedAt    = Instant.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public Instant getEmailVerificationExpiresAt() { return emailVerificationExpiresAt; }
    public void setEmailVerificationExpiresAt(Instant emailVerificationExpiresAt) {
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
    }

    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
