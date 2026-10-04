package com.navan.billing.web;

import com.navan.billing.dto.CreateTransactionRequest;
import com.navan.billing.dto.PatchTransactionRequest;
import com.navan.billing.dto.PageResponse;
import com.navan.billing.dto.TransactionSummary;
import com.navan.billing.dto.TransactionView;
import com.navan.billing.domain.Ids;
import com.navan.billing.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionView> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    @GetMapping("/{id}")
    public TransactionView get(@PathVariable String id) {
        return transactionService.get(Ids.parseTxn(id));
    }

    @GetMapping
    public PageResponse<TransactionSummary> list(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return transactionService.list(limit, offset);
    }

    @PatchMapping("/{id}")
    public TransactionView patch(@PathVariable String id, @Valid @RequestBody PatchTransactionRequest request) {
        return transactionService.patch(Ids.parseTxn(id), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        transactionService.delete(Ids.parseTxn(id));
        return ResponseEntity.noContent().build();
    }
}
