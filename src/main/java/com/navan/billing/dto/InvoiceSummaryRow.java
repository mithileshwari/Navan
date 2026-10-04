package com.navan.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceSummaryRow(
        Long id,
        String currency,
        BigDecimal total,
        Long lineCount,
        Instant createdAt) {
}
