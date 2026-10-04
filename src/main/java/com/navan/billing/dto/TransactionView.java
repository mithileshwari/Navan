package com.navan.billing.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TransactionView(
        String id,
        String type,
        String externalId,
        String currency,
        Instant occurredAt,
        BigDecimal total,
        List<TaxLineDto> taxLines,
        List<FeeLineDto> feeLines,
        JsonNode metadata,
        Instant createdAt,
        Instant updatedAt) {
}
