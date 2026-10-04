package com.navan.billing.dto;

import com.navan.billing.domain.Ids;
import com.navan.billing.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public record InvoiceRequest(String transactionId, List<String> transactionIds) {
    public List<Long> ids() {
        if ((transactionId == null) == (transactionIds == null)
                || (transactionIds != null && transactionIds.isEmpty())) {
            throw invalidIds();
        }
        try {
            return transactionId != null
                    ? List.of(Ids.parseTxn(transactionId))
                    : transactionIds.stream().map(Ids::parseTxn).toList();
        } catch (IllegalArgumentException exception) {
            throw invalidIds();
        }
    }

    private ApiException invalidIds() {
        return new ApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                "Provide transaction_id or a non-empty transaction_ids list of valid transaction IDs", Map.of());
    }
}
