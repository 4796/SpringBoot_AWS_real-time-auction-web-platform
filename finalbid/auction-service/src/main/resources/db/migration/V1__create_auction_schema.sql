-- V1__create_auction_schema.sql
-- Creates the auction_schema, auctions and bids tables for FinalBid.

CREATE SCHEMA IF NOT EXISTS auction_schema;

-- ── Enums ────────────────────────────────────────────────────────────────────

CREATE TYPE auction_schema.auction_category AS ENUM (
    'ELECTRONICS', 'FASHION', 'HOME', 'TOYS', 'SPORTS', 'ART', 'BOOKS', 'OTHER'
);

CREATE TYPE auction_schema.auction_condition AS ENUM (
    'NEW', 'USED_LIKE_NEW', 'USED'
);

CREATE TYPE auction_schema.auction_duration_option AS ENUM (
    'MIN_1', 'MIN_5', 'MIN_10', 'MIN_30', 'HOUR_1', 'HOUR_2'
);

CREATE TYPE auction_schema.auction_status AS ENUM (
    'ACTIVE', 'ENDED'
);

-- ── Table: auctions ──────────────────────────────────────────────────────────

CREATE TABLE auction_schema.auctions (
    id                UUID           NOT NULL DEFAULT gen_random_uuid(),
    seller_id         UUID           NOT NULL,
    title             VARCHAR(100)   NOT NULL,
    description       TEXT           NOT NULL,
    category          auction_schema.auction_category NOT NULL,
    condition         auction_schema.auction_condition NOT NULL,
    start_price       DECIMAL(19, 2) NOT NULL,
    current_price     DECIMAL(19, 2) NOT NULL,
    bid_count         INT            NOT NULL DEFAULT 0,
    duration_option   auction_schema.auction_duration_option NOT NULL,
    end_at            TIMESTAMP      NOT NULL,
    status            auction_schema.auction_status NOT NULL DEFAULT 'ACTIVE',
    image_url         VARCHAR(512),
    winner_id         UUID,
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT now(),
    version           BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT pk_auctions PRIMARY KEY (id)
);

-- ── Table: bids ──────────────────────────────────────────────────────────────

CREATE TABLE auction_schema.bids (
    id          UUID           NOT NULL DEFAULT gen_random_uuid(),
    auction_id  UUID           NOT NULL,
    bidder_id   UUID           NOT NULL,
    amount      DECIMAL(19, 2) NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT pk_bids PRIMARY KEY (id),
    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auction_schema.auctions(id) ON DELETE CASCADE
);

-- ── Trigger: auto-update updated_at ──────────────────────────────────────────

CREATE OR REPLACE FUNCTION auction_schema.set_updated_at()
    RETURNS TRIGGER LANGUAGE plpgsql AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_auctions_updated_at
    BEFORE UPDATE ON auction_schema.auctions
    FOR EACH ROW EXECUTE PROCEDURE auction_schema.set_updated_at();

-- ── Indexes ──────────────────────────────────────────────────────────────────

CREATE INDEX idx_auctions_seller_id ON auction_schema.auctions(seller_id);
CREATE INDEX idx_auctions_status    ON auction_schema.auctions(status);
CREATE INDEX idx_auctions_end_at    ON auction_schema.auctions(end_at);
CREATE INDEX idx_bids_auction_id    ON auction_schema.bids(auction_id);
CREATE INDEX idx_bids_bidder_id     ON auction_schema.bids(bidder_id);
