package com.navan.billing.domain;

public enum FeeKind {
    trip_fee,
    agent_call_fee;

    public String wire() {
        return name();
    }

    public static FeeKind fromWire(String value) {
        for (FeeKind kind : values()) {
            if (kind.name().equals(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown fee kind: " + value);
    }
}
