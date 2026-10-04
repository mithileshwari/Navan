package com.navan.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSummary(
        String id,
        String type,
        String externalId,
        String currency,
        BigDecimal total,
        Instant occurredAt) {
}
