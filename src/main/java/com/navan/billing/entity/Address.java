package com.navan.billing.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public record Address(
        @NotBlank String street,
        @NotBlank String city,
        @NotBlank String country) {
}
