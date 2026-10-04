package com.navan.billing.dto;

import com.navan.billing.domain.TxnType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSummaryRow(
        Long id,
        TxnType type,
        String externalId,
        String currency,
        BigDecimal total,
        Instant occurredAt) {
}
