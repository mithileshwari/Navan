package com.navan.billing.service;

import com.navan.billing.domain.TxnType;
import com.navan.billing.domain.Ids;
import com.navan.billing.dto.CreateTransactionRequest;
import com.navan.billing.dto.PatchTransactionRequest;
import com.navan.billing.dto.PageResponse;
import com.navan.billing.dto.TransactionSummary;
import com.navan.billing.dto.TransactionView;
import com.navan.billing.dto.TransactionSummaryRow;
import com.navan.billing.entity.MetadataEntity;
import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.exception.ApiException;
import com.navan.billing.metadata.MetadataRegistry;
import com.navan.billing.repo.InvoiceLineRepository;
import com.navan.billing.repo.TransactionRepository;
import com.navan.billing.repo.OffsetLimit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TransactionService {
    private final TransactionRepository transactions;
    private final InvoiceLineRepository invoiceLines;
    private final MetadataRegistry metadataRegistry;
    private final TransactionMapper mapper;
    private final Validator validator;

    @PersistenceContext
    private EntityManager entityManager;

    public TransactionService(TransactionRepository transactions, InvoiceLineRepository invoiceLines,
                              MetadataRegistry metadataRegistry, TransactionMapper mapper, Validator validator) {
        this.transactions = transactions;
        this.invoiceLines = invoiceLines;
        this.metadataRegistry = metadataRegistry;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Transactional
    public TransactionView create(CreateTransactionRequest request) {
        TxnType type = TxnType.parse(request.type());
        if (transactions.findIdByExternalId(request.externalId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate_external_id", "external_id already exists", Map.of());
        }

        TransactionEntity entity = mapper.toEntity(request, type);
        validate(entity);

        try {
            return mapper.toView(transactions.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate_external_id", "external_id already exists", Map.of());
        }
    }

    @Transactional(readOnly = true)
    public TransactionView get(long id) {
        TransactionEntity entity = transactions.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Transaction not found", Map.of()));
        return mapper.toView(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionSummary> list(int limit, int offset) {
        if (limit < 1 || limit > 100 || offset < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "limit must be between 1 and 100 and offset must not be negative", Map.of());
        }

        List<TransactionSummaryRow> rows = transactions.listSummaries(new OffsetLimit(offset, limit + 1)).getContent();
        boolean hasMore = rows.size() > limit;
        List<TransactionSummary> items = rows.stream().limit(limit)
                .map(row -> new TransactionSummary(Ids.txn(row.id()), row.type().wire(),
                        row.externalId(), row.currency(), row.total(), row.occurredAt()))
                .toList();
        return new PageResponse<>(items, limit, offset, hasMore);
    }

    @Transactional
    public TransactionView patch(long id, PatchTransactionRequest patch) {
        TransactionEntity entity = transactions.lockById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Transaction not found", Map.of()));

        if (invoiceLines.existsByTransactionId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "transaction_invoiced",
                    "An invoiced transaction cannot be updated", Map.of());
        }

        MetadataEntity replacement = patch.metadata() == null
                ? null
                : metadataRegistry.parse(entity.getType(), patch.metadata());

        mapper.applyEnvelopePatch(entity, patch);
        if (replacement != null) {
            replacement.setId(id);
            replacement.setTransaction(entity);
            entity.setMetadata(entityManager.merge(replacement));
        }
        validate(entity);
        return mapper.toView(entity);
    }

    @Transactional
    public void delete(long id) {
        TransactionEntity entity = transactions.lockById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Transaction not found", Map.of()));
        if (invoiceLines.existsByTransactionId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "transaction_invoiced",
                    "An invoiced transaction cannot be deleted", Map.of());
        }
        transactions.delete(entity);
    }

    private void validate(TransactionEntity entity) {
        Set<ConstraintViolation<TransactionEntity>> violations = validator.validate(entity);
        if (violations.isEmpty()) {
            return;
        }
        TreeSet<String> messages = new TreeSet<>();
        for (ConstraintViolation<TransactionEntity> violation : violations) {
            messages.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_request", String.join("; ", messages), Map.of());
    }

}
