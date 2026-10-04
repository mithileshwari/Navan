package com.navan.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record InvoiceView(
        String id,
        String currency,
        BigDecimal total,
        List<InvoiceLineView> lines,
        List<TaxRollupLine> taxRollup,
        List<FeeRollupLine> feeRollup,
        Instant createdAt,
        Instant updatedAt) {
}
