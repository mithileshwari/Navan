package com.navan.billing.dto;

import java.math.BigDecimal;

public record TaxRollupLine(String name, BigDecimal rate, BigDecimal amount) {
}
