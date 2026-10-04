package com.navan.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navan.billing.domain.Ids;
import com.navan.billing.dto.FeeLineDto;
import com.navan.billing.dto.InvoiceLineView;
import com.navan.billing.dto.InvoiceView;
import com.navan.billing.dto.TaxLineDto;
import com.navan.billing.entity.InvoiceEntity;
import com.navan.billing.entity.InvoiceLineEntity;
import com.navan.billing.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InvoiceMapper {
    private final ObjectMapper objectMapper;

    public InvoiceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public InvoiceView toView(InvoiceEntity invoice, List<InvoiceLineEntity> entities) {
        List<InvoiceLineView> lines = entities.stream().map(this::toLineView).toList();
        return new InvoiceView(Ids.inv(invoice.getId()), invoice.getCurrency(), invoice.getTotal(), lines,
                RollupCalculator.tax(lines), RollupCalculator.fee(lines), invoice.getCreatedAt(), invoice.getUpdatedAt());
    }

    private InvoiceLineView toLineView(InvoiceLineEntity line) {
        JsonNode metadata;
        try {
            metadata = objectMapper.readTree(line.getMetadataJson());
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "snapshot_error",
                    "Stored invoice metadata is invalid", Map.of());
        }
        List<TaxLineDto> taxes = line.getTaxLines().stream()
                .map(tax -> new TaxLineDto(tax.getName(), tax.getRate(), tax.getAmount()))
                .toList();
        List<FeeLineDto> fees = line.getFeeLines().stream()
                .map(fee -> new FeeLineDto(fee.getName(), fee.getAmount()))
                .toList();
        return new InvoiceLineView(Ids.txn(line.getTransactionId()), line.getType(), line.getExternalId(),
                line.getCurrency(), line.getOccurredAt(), line.getTotal(), taxes, fees, metadata);
    }
}
