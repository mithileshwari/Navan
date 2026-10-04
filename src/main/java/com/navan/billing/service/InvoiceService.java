package com.navan.billing.service;

import com.navan.billing.domain.BillingUtils;
import com.navan.billing.domain.Ids;
import com.navan.billing.dto.InvoiceSummary;
import com.navan.billing.dto.InvoiceSummaryRow;
import com.navan.billing.dto.InvoiceRequest;
import com.navan.billing.dto.InvoiceView;
import com.navan.billing.dto.PageResponse;
import com.navan.billing.dto.TransactionInvoiceRow;
import com.navan.billing.entity.InvoiceEntity;
import com.navan.billing.entity.InvoiceLineEntity;
import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.exception.ApiException;
import com.navan.billing.repo.InvoiceLineRepository;
import com.navan.billing.repo.InvoiceRepository;
import com.navan.billing.repo.OffsetLimit;
import com.navan.billing.repo.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class InvoiceService {
    private final InvoiceRepository invoices;
    private final InvoiceLineRepository invoiceLines;
    private final TransactionRepository transactions;
    private final SnapshotBuilder snapshots;
    private final InvoiceMapper mapper;

    public InvoiceService(InvoiceRepository invoices, InvoiceLineRepository invoiceLines,
                          TransactionRepository transactions, SnapshotBuilder snapshots, InvoiceMapper mapper) {
        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.transactions = transactions;
        this.snapshots = snapshots;
        this.mapper = mapper;
    }

    @Transactional
    public InvoiceView create(InvoiceRequest request) {
        List<Long> ids = request.ids();
        List<InvoiceLineEntity> lines = lockAndSnapshot(ids, null);
        String currency = lines.get(0).getCurrency();
        var total = BillingUtils.sum(lines.stream().map(InvoiceLineEntity::getTotal).toList());
        Instant now = Instant.now();

        InvoiceEntity invoice = invoices.saveAndFlush(new InvoiceEntity(currency, total, now, now));
        lines.forEach(line -> line.setInvoiceId(invoice.getId()));
        List<InvoiceLineEntity> savedLines = invoiceLines.saveAll(lines);
        return mapper.toView(invoice, savedLines);
    }

    @Transactional(readOnly = true)
    public InvoiceView get(long id) {
        InvoiceEntity invoice = invoices.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Invoice not found", Map.of()));
        List<InvoiceLineEntity> lines = invoiceLines.findByInvoiceIdOrderById(id);
        return mapper.toView(invoice, lines);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceSummary> list(int limit, int offset) {
        if (limit < 1 || limit > 100 || offset < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_request",
                    "limit must be between 1 and 100 and offset must not be negative", Map.of());
        }

        List<InvoiceSummaryRow> rows = invoices.listSummaries(new OffsetLimit(offset, limit + 1)).getContent();
        boolean hasMore = rows.size() > limit;
        List<InvoiceSummary> items = rows.stream().limit(limit)
                .map(row -> new InvoiceSummary(Ids.inv(row.id()), row.currency(), row.total(),
                        row.lineCount(), row.createdAt()))
                .toList();
        return new PageResponse<>(items, limit, offset, hasMore);
    }

    @Transactional
    public InvoiceView patch(long id, InvoiceRequest request) {
        List<Long> ids = request.ids();
        InvoiceEntity invoice = invoices.lockById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Invoice not found", Map.of()));

        List<InvoiceLineEntity> replacementLines = lockAndSnapshot(ids, id);
        String currency = replacementLines.get(0).getCurrency();
        var total = BillingUtils.sum(replacementLines.stream().map(InvoiceLineEntity::getTotal).toList());

        invoiceLines.deleteByInvoice(id);

        invoice.setCurrency(currency);
        invoice.setTotal(total);
        invoice.setUpdatedAt(Instant.now());
        invoice = invoices.save(invoice);
        replacementLines.forEach(line -> line.setInvoiceId(id));
        List<InvoiceLineEntity> savedLines = invoiceLines.saveAll(replacementLines);
        return mapper.toView(invoice, savedLines);
    }

    @Transactional
    public void delete(long id) {
        InvoiceEntity invoice = invoices.lockById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "not_found", "Invoice not found", Map.of()));
        invoices.delete(invoice);
    }

    private List<InvoiceLineEntity> lockAndSnapshot(List<Long> ids, Long currentInvoiceId) {
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate_transaction_id",
                    "A transaction ID was provided more than once", Map.of());
        }

        List<Long> lockOrder = ids.stream().sorted().toList();
        Map<Long, TransactionEntity> loaded = new HashMap<>();
        transactions.lockAllByIds(lockOrder).forEach(transaction -> loaded.put(transaction.getId(), transaction));
        for (Long id : lockOrder) {
            if (!loaded.containsKey(id)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "not_found", "Transaction not found", Map.of());
            }
        }

        List<TransactionInvoiceRow> memberships = invoiceLines.findInvoiceMembershipsByTransactionIds(lockOrder);
        for (TransactionInvoiceRow membership : memberships) {
            boolean belongsToAnotherInvoice = !Objects.equals(membership.invoiceId(), currentInvoiceId);
            if (belongsToAnotherInvoice) {
                throw new ApiException(HttpStatus.CONFLICT, "transaction_already_invoiced",
                        "A transaction can belong to only one invoice", Map.of());
            }
        }

        Set<String> currencies = new HashSet<>();
        loaded.values().forEach(transaction -> currencies.add(transaction.getCurrency()));
        if (currencies.size() > 1) {
            throw new ApiException(HttpStatus.CONFLICT, "mixed_currency",
                    "All transactions in an invoice must use the same currency", Map.of());
        }

        List<InvoiceLineEntity> lines = new ArrayList<>(ids.size());
        for (Long id : ids) {
            lines.add(snapshots.build(loaded.get(id)));
        }
        return lines;
    }
}
