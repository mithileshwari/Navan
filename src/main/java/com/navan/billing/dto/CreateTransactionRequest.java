package com.navan.billing.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreateTransactionRequest(
        @NotBlank
        String type,
        @NotBlank
        @Size(max = 128)
        String externalId,
        @NotNull
        @Pattern(regexp = "[A-Z]{3}")
        String currency,
        @NotNull
        Instant occurredAt,
        @NotNull
        @Digits(integer = 15, fraction = 4)
        BigDecimal total,
        @NotNull
        @Valid
        List<TaxLineDto> taxLines,
        @NotNull
        @Valid
        List<FeeLineDto> feeLines,
        JsonNode metadata) {
}
