package com.navan.billing.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PatchTransactionRequest(
        @Digits(integer = 15, fraction = 4)
        BigDecimal total,
        Instant occurredAt,
        @Valid
        List<TaxLineDto> taxLines,
        @Valid
        List<FeeLineDto> feeLines,
        JsonNode metadata) {
}
