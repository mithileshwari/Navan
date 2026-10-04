package com.navan.billing.repo;

import com.navan.billing.entity.TransactionEntity;
import com.navan.billing.dto.TransactionSummaryRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransactionEntity t where t.id = :id")
    Optional<TransactionEntity> lockById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransactionEntity t where t.id in :ids order by t.id")
    List<TransactionEntity> lockAllByIds(@Param("ids") List<Long> ids);

    @Query("select t.id from TransactionEntity t where t.externalId = :externalId")
    Optional<Long> findIdByExternalId(@Param("externalId") String externalId);

    @Query("select new com.navan.billing.dto.TransactionSummaryRow(t.id, t.type, t.externalId, "
            + "t.currency, t.total, t.occurredAt) from TransactionEntity t order by t.id")
    Slice<TransactionSummaryRow> listSummaries(Pageable pageable);
}
