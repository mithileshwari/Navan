package com.navan.billing.service;

import com.navan.billing.dto.FeeRollupLine;
import com.navan.billing.dto.InvoiceLineView;
import com.navan.billing.dto.TaxRollupLine;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RollupCalculator {
    private RollupCalculator() {
    }

    public static List<TaxRollupLine> tax(List<InvoiceLineView> lines) {
        Map<TaxKey, TaxTotal> totals = new LinkedHashMap<>();
        for (InvoiceLineView line : lines) {
            for (var tax : line.taxLines()) {
                TaxKey key = new TaxKey(tax.name(), tax.rate() == null ? "" : tax.rate().stripTrailingZeros().toPlainString());
                totals.computeIfAbsent(key, ignored -> new TaxTotal(tax.rate()))
                        .add(tax.amount());
            }
        }
        return totals.entrySet().stream()
                .map(entry -> new TaxRollupLine(entry.getKey().name(), entry.getValue().rate, entry.getValue().amount))
                .toList();
    }

    public static List<FeeRollupLine> fee(List<InvoiceLineView> lines) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (InvoiceLineView line : lines) {
            for (var fee : line.feeLines()) {
                totals.merge(fee.name(), fee.amount(), BigDecimal::add);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> new FeeRollupLine(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static BigDecimal total(List<InvoiceLineView> lines) {
        return lines.stream().map(InvoiceLineView::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record TaxKey(String name, String rate) {
    }

    private static final class TaxTotal {
        private final BigDecimal rate;
        private BigDecimal amount = BigDecimal.ZERO;

        private TaxTotal(BigDecimal rate) {
            this.rate = rate;
        }

        private void add(BigDecimal value) {
            amount = amount.add(value);
        }
    }
}
