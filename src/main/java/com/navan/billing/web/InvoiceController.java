package com.navan.billing.web;

import com.navan.billing.domain.Ids;
import com.navan.billing.dto.InvoiceRequest;
import com.navan.billing.dto.InvoiceSummary;
import com.navan.billing.dto.InvoiceView;
import com.navan.billing.dto.PageResponse;
import com.navan.billing.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<InvoiceView> create(@RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @GetMapping("/{id}")
    public InvoiceView get(@PathVariable String id) {
        return invoiceService.get(Ids.parseInv(id));
    }

    @GetMapping
    public PageResponse<InvoiceSummary> list(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return invoiceService.list(limit, offset);
    }

    @PatchMapping("/{id}")
    public InvoiceView patch(@PathVariable String id, @RequestBody InvoiceRequest request) {
        return invoiceService.patch(Ids.parseInv(id), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        invoiceService.delete(Ids.parseInv(id));
        return ResponseEntity.noContent().build();
    }
}
