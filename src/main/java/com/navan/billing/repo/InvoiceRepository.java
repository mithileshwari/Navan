package com.navan.billing.repo;

import com.navan.billing.entity.InvoiceEntity;
import com.navan.billing.dto.InvoiceSummaryRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invoice from InvoiceEntity invoice where invoice.id = :id")
    Optional<InvoiceEntity> lockById(@Param("id") Long id);

    @Query("select new com.navan.billing.dto.InvoiceSummaryRow(invoice.id, invoice.currency, invoice.total, "
            + "(select count(line) from InvoiceLineEntity line where line.invoiceId = invoice.id), invoice.createdAt) "
            + "from InvoiceEntity invoice order by invoice.id")
    Slice<InvoiceSummaryRow> listSummaries(Pageable pageable);
}
