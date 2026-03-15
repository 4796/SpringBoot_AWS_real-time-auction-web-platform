package com.finalbid.auction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to auction_schema.bids table.
 */
@Entity
@Table(name = "bids", schema = "auction_schema")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false, updatable = false)
    private Auction auction;

    @Column(name = "bidder_id", nullable = false, updatable = false)
    private UUID bidderId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Bid() {}

    public Bid(Auction auction, UUID bidderId, BigDecimal amount) {
        this.auction  = auction;
        this.bidderId = bidderId;
        this.amount   = amount;
        this.createdAt = Instant.now();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public Auction getAuction() { return auction; }

    public UUID getBidderId() { return bidderId; }

    public BigDecimal getAmount() { return amount; }

    public Instant getCreatedAt() { return createdAt; }
}
