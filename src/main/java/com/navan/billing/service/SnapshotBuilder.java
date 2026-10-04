package com.navan.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navan.billing.entity.FeeLineEntity;
import com.navan.billing.entity.InvoiceLineEntity;
import com.navan.billing.entity.InvoiceLineFeeEntity;
import com.navan.billing.entity.InvoiceLineTaxEntity;
import com.navan.billing.entity.TaxLineEntity;
import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.exception.ApiException;
import com.navan.billing.metadata.MetadataRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SnapshotBuilder {
    private final MetadataRegistry metadataRegistry;
    private final ObjectMapper objectMapper;

    public SnapshotBuilder(MetadataRegistry metadataRegistry, ObjectMapper objectMapper) {
        this.metadataRegistry = metadataRegistry;
        this.objectMapper = objectMapper;
    }

    public InvoiceLineEntity build(TransactionEntity transaction) {
        InvoiceLineEntity line = new InvoiceLineEntity();
        line.setTransactionId(transaction.getId());
        line.setType(transaction.getType().wire());
        line.setExternalId(transaction.getExternalId());
        line.setCurrency(transaction.getCurrency());
        line.setOccurredAt(transaction.getOccurredAt());
        line.setTotal(transaction.getTotal());
        try {
            line.setMetadataJson(objectMapper.writeValueAsString(metadataRegistry.toJson(transaction.getMetadata())));
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "snapshot_error",
                    "Could not serialize transaction metadata", Map.of());
        }
        for (TaxLineEntity tax : transaction.getTaxLines()) {
            line.addTaxLine(new InvoiceLineTaxEntity(tax.getName(), tax.getRate(), tax.getAmount()));
        }
        for (FeeLineEntity fee : transaction.getFeeLines()) {
            line.addFeeLine(new InvoiceLineFeeEntity(fee.getName(), fee.getAmount()));
        }
        return line;
    }
}
