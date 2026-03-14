-- V1__create_user_schema.sql
-- Creates the user_schema and users table for FinalBid user-service.

CREATE SCHEMA IF NOT EXISTS user_schema;

-- ── Enums ────────────────────────────────────────────────────────────────────

CREATE TYPE user_schema.user_status AS ENUM (
    'PENDING_VERIFICATION',
    'ACTIVE',
    'BANNED'
);

CREATE TYPE user_schema.user_role AS ENUM (
    'USER'
);

-- ── Table: users ─────────────────────────────────────────────────────────────

CREATE TABLE user_schema.users (
    id                            UUID         NOT NULL DEFAULT gen_random_uuid(),
    username                      VARCHAR(50)  NOT NULL,
    email                         VARCHAR(255) NOT NULL,
    password_hash                 VARCHAR(255) NOT NULL,
    status                        user_schema.user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    role                          user_schema.user_role   NOT NULL DEFAULT 'USER',
    profile_picture_url           VARCHAR(512),
    email_verification_token      VARCHAR(255),
    email_verification_expires_at TIMESTAMP,
    refresh_token_hash            VARCHAR(255),
    created_at                    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_users             PRIMARY KEY (id),
    CONSTRAINT uq_users_username    UNIQUE (username),
    CONSTRAINT uq_users_email       UNIQUE (email)
);

-- ── Trigger: auto-update updated_at ──────────────────────────────────────────

CREATE OR REPLACE FUNCTION user_schema.set_updated_at()
    RETURNS TRIGGER LANGUAGE plpgsql AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON user_schema.users
    FOR EACH ROW EXECUTE PROCEDURE user_schema.set_updated_at();

-- ── Indexes ───────────────────────────────────────────────────────────────────

CREATE INDEX idx_users_email_verification_token
    ON user_schema.users (email_verification_token)
    WHERE email_verification_token IS NOT NULL;
