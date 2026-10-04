package com.navan.billing.service;

import com.navan.billing.domain.Ids;
import com.navan.billing.domain.TxnType;
import com.navan.billing.dto.CreateTransactionRequest;
import com.navan.billing.dto.FeeLineDto;
import com.navan.billing.dto.PatchTransactionRequest;
import com.navan.billing.dto.TaxLineDto;
import com.navan.billing.dto.TransactionView;
import com.navan.billing.entity.FeeLineEntity;
import com.navan.billing.entity.TaxLineEntity;
import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.metadata.MetadataRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class TransactionMapper {
    private final MetadataRegistry metadataRegistry;

    public TransactionMapper(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    public TransactionEntity toEntity(CreateTransactionRequest request, TxnType type) {
        Instant now = Instant.now();
        TransactionEntity entity = new TransactionEntity();
        entity.setExternalId(request.externalId());
        entity.setType(type);
        entity.setCurrency(request.currency());
        entity.setOccurredAt(request.occurredAt());
        entity.setTotal(request.total());
        entity.replaceTaxLines(toTaxLines(request.taxLines()));
        entity.replaceFeeLines(toFeeLines(request.feeLines()));
        entity.setMetadata(metadataRegistry.parse(type, request.metadata()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    public void applyEnvelopePatch(TransactionEntity entity, PatchTransactionRequest patch) {
        if (patch.total() != null) entity.setTotal(patch.total());
        if (patch.occurredAt() != null) entity.setOccurredAt(patch.occurredAt());
        if (patch.taxLines() != null) entity.replaceTaxLines(toTaxLines(patch.taxLines()));
        if (patch.feeLines() != null) entity.replaceFeeLines(toFeeLines(patch.feeLines()));
        entity.setUpdatedAt(Instant.now());
    }

    public TransactionView toView(TransactionEntity entity) {
        return new TransactionView(
                Ids.txn(entity.getId()),
                entity.getType().wire(),
                entity.getExternalId(),
                entity.getCurrency(),
                entity.getOccurredAt(),
                entity.getTotal(),
                toTaxDtos(entity.getTaxLines()),
                toFeeDtos(entity.getFeeLines()),
                metadataRegistry.toJson(entity.getMetadata()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private List<TaxLineEntity> toTaxLines(List<TaxLineDto> lines) {
        return lines == null ? null : lines.stream().map(line -> {
            TaxLineEntity entity = new TaxLineEntity();
            entity.setName(line.name());
            entity.setRate(line.rate());
            entity.setAmount(line.amount());
            return entity;
        }).toList();
    }

    private List<FeeLineEntity> toFeeLines(List<FeeLineDto> lines) {
        return lines == null ? null : lines.stream().map(line -> {
            FeeLineEntity entity = new FeeLineEntity();
            entity.setName(line.name());
            entity.setAmount(line.amount());
            return entity;
        }).toList();
    }

    private List<TaxLineDto> toTaxDtos(List<TaxLineEntity> lines) {
        return lines == null ? null : lines.stream()
                .map(line -> new TaxLineDto(line.getName(), line.getRate(), line.getAmount()))
                .toList();
    }

    private List<FeeLineDto> toFeeDtos(List<FeeLineEntity> lines) {
        return lines == null ? null : lines.stream()
                .map(line -> new FeeLineDto(line.getName(), line.getAmount()))
                .toList();
    }
}
