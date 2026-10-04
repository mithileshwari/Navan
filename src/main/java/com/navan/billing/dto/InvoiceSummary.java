package com.navan.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceSummary(
        String id,
        String currency,
        BigDecimal total,
        long lineCount,
        Instant createdAt) {
}
