package com.navan.billing.dto;

import java.math.BigDecimal;

public record FeeRollupLine(String name, BigDecimal amount) {
}
