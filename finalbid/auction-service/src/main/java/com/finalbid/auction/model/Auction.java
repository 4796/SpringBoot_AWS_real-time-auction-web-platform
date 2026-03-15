package com.finalbid.auction.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to auction_schema.auctions table.
 */
@Entity
@Table(name = "auctions", schema = "auction_schema")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "category", nullable = false, columnDefinition = "auction_schema.auction_category")
    private Category category;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "condition", nullable = false, columnDefinition = "auction_schema.auction_condition")
    private Condition condition;

    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "current_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "bid_count", nullable = false)
    private int bidCount = 0;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "duration_option", nullable = false, columnDefinition = "auction_schema.auction_duration_option")
    private DurationOption durationOption;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", nullable = false, columnDefinition = "auction_schema.auction_status")
    private AuctionStatus status = AuctionStatus.ACTIVE;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    // ── Constructors ──────────────────────────────────────────────────────────

    protected Auction() {}

    public Auction(UUID sellerId, String title, String description, Category category,
                   Condition condition, BigDecimal startPrice, DurationOption durationOption, Instant endAt) {
        this.sellerId       = sellerId;
        this.title          = title;
        this.description    = description;
        this.category       = category;
        this.condition      = condition;
        this.startPrice     = startPrice;
        this.currentPrice   = startPrice;
        this.durationOption = durationOption;
        this.endAt          = endAt;
        this.status         = AuctionStatus.ACTIVE;
        this.createdAt      = Instant.now();
        this.updatedAt      = Instant.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public UUID getSellerId() { return sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public BigDecimal getStartPrice() { return startPrice; }
    public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public int getBidCount() { return bidCount; }
    public void setBidCount(int bidCount) { this.bidCount = bidCount; }

    public DurationOption getDurationOption() { return durationOption; }
    public void setDurationOption(DurationOption durationOption) { this.durationOption = durationOption; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public UUID getWinnerId() { return winnerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
}
