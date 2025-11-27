package com.example.banksys.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferHistoryItem {

    private String fromUsername;
    private String toUsername;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime createdAt;

    public TransferHistoryItem() {
    }

    public TransferHistoryItem(String fromUsername,
                               String toUsername,
                               BigDecimal amount,
                               String currency,
                               String description,
                               LocalDateTime createdAt) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
