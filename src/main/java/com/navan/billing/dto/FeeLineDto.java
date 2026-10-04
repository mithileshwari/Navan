package com.navan.billing.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FeeLineDto(
        @NotBlank @Size(max = 64) String name,
        @NotNull @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
