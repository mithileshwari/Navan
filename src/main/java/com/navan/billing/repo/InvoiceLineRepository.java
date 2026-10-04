package com.navan.billing.repo;

import com.navan.billing.entity.InvoiceLineEntity;
import com.navan.billing.dto.TransactionInvoiceRow;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLineEntity, Long> {
    boolean existsByTransactionId(Long transactionId);

    @Query("select new com.navan.billing.dto.TransactionInvoiceRow(line.transactionId, line.invoiceId) "
            + "from InvoiceLineEntity line where line.transactionId in :transactionIds")
    List<TransactionInvoiceRow> findInvoiceMembershipsByTransactionIds(
            @Param("transactionIds") List<Long> transactionIds);

    List<InvoiceLineEntity> findByInvoiceIdOrderById(Long invoiceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from InvoiceLineEntity line where line.invoiceId = :invoiceId")
    int deleteByInvoice(@Param("invoiceId") Long invoiceId);
}
