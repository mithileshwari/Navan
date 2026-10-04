package com.navan.billing.entity;

import com.navan.billing.domain.TxnType;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 128)
    private String externalId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TxnType type;

    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    private String currency;

    @NotNull
    private Instant occurredAt;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    private BigDecimal total;

    @NotNull
    @Valid
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<TaxLineEntity> taxLines = new ArrayList<>();

    @NotNull
    @Valid
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<FeeLineEntity> feeLines = new ArrayList<>();

    @NotNull
    @Valid
    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = jakarta.persistence.FetchType.EAGER, optional = false)
    private MetadataEntity metadata;

    private Instant createdAt;

    private Instant updatedAt;

    public TransactionEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public TxnType getType() { return type; }
    public void setType(TxnType type) { this.type = type; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<TaxLineEntity> getTaxLines() { return taxLines; }
    public void replaceTaxLines(List<TaxLineEntity> lines) {
        if (lines == null) {
            taxLines = null;
            return;
        }
        taxLines.clear();
        lines.forEach(line -> {
            line.setTransaction(this);
            taxLines.add(line);
        });
    }
    public List<FeeLineEntity> getFeeLines() { return feeLines; }
    public void replaceFeeLines(List<FeeLineEntity> lines) {
        if (lines == null) {
            feeLines = null;
            return;
        }
        feeLines.clear();
        lines.forEach(line -> {
            line.setTransaction(this);
            feeLines.add(line);
        });
    }
    public MetadataEntity getMetadata() { return metadata; }
    public void setMetadata(MetadataEntity metadata) {
        this.metadata = metadata;
        if (metadata != null) {
            metadata.setTransaction(this);
        }
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @AssertTrue(message = "type must match metadata")
    public boolean isTypeMatchingMetadata() {
        return metadata == null || type == null || metadata.type() == type;
    }
}
