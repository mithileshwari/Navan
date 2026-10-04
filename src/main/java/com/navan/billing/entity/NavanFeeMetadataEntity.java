package com.navan.billing.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.navan.billing.domain.FeeKind;
import com.navan.billing.domain.TxnType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "navan_fee_details")
@PrimaryKeyJoinColumn(name = "transaction_id")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class NavanFeeMetadataEntity extends MetadataEntity {
    @NotNull
    @Enumerated(EnumType.STRING)
    private FeeKind feeKind;

    private String description;

    private String relatedExternalId;

    public NavanFeeMetadataEntity() {
    }

    @Override
    public TxnType type() { return TxnType.navan_fee; }

    public FeeKind getFeeKind() { return feeKind; }
    public void setFeeKind(FeeKind feeKind) { this.feeKind = feeKind; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRelatedExternalId() { return relatedExternalId; }
    public void setRelatedExternalId(String relatedExternalId) { this.relatedExternalId = relatedExternalId; }
}
