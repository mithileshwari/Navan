package com.navan.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public final class BillingUtils {
    private BillingUtils() {
    }

    public static BigDecimal toWire(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal sum(Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
