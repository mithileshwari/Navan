package com.navan.billing.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxLineDto(
        @NotBlank @Size(max = 64) String name,
        @DecimalMin("0") @DecimalMax("1") @Digits(integer = 1, fraction = 6) BigDecimal rate,
        @NotNull @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
