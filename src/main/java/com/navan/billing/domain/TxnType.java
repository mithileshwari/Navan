package com.navan.billing.domain;

import com.navan.billing.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public enum TxnType {
    flight,
    hotel,
    rail,
    navan_fee;

    public String wire() {
        return name();
    }

    public static TxnType parse(String value) {
        for (TxnType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "unknown_type", "Unknown transaction type: " + value, Map.of());
    }
}
