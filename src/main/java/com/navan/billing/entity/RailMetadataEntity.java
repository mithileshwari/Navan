package com.navan.billing.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.navan.billing.domain.TxnType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rail_details")
@PrimaryKeyJoinColumn(name = "transaction_id")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RailMetadataEntity extends MetadataEntity {
    @NotEmpty
    @Valid
    @ElementCollection
    @CollectionTable(name = "rail_legs", joinColumns = @JoinColumn(name = "transaction_id"))
    private List<RailLeg> legs = new ArrayList<>();

    public RailMetadataEntity() {
    }

    @Override
    public TxnType type() { return TxnType.rail; }

    public List<RailLeg> getLegs() { return legs; }
    public void setLegs(List<RailLeg> legs) { this.legs = legs; }
}
