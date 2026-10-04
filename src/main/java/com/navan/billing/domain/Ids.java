package com.navan.billing.domain;

public final class Ids {
    private Ids() {
    }

    public static String txn(long id) {
        return "txn_" + id;
    }

    public static long parseTxn(String value) {
        return parse(value, "txn_");
    }

    public static String inv(long id) {
        return "inv_" + id;
    }

    public static long parseInv(String value) {
        return parse(value, "inv_");
    }

    private static long parse(String value, String prefix) {
        if (value == null || !value.startsWith(prefix) || value.length() == prefix.length()) {
            throw new IllegalArgumentException("Malformed id: " + value);
        }
        String suffix = value.substring(prefix.length());
        if (!suffix.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Malformed id: " + value);
        }
        try {
            return Long.parseLong(suffix);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed id: " + value, e);
        }
    }
}
