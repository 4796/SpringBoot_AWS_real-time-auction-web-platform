package com.finalbid.auction.dto;

import com.finalbid.auction.model.Category;
import com.finalbid.auction.model.Condition;
import com.finalbid.auction.model.DurationOption;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AuctionCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Category is required")
    Category category,

    @NotNull(message = "Condition is required")
    Condition condition,

    @NotNull(message = "Start price is required")
    @DecimalMin(value = "0.01", message = "Start price must be at least 0.01")
    BigDecimal startPrice,

    @NotNull(message = "Duration option is required")
    DurationOption durationOption
) {}
