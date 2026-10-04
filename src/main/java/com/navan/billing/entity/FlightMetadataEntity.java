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
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_details")
@PrimaryKeyJoinColumn(name = "transaction_id")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FlightMetadataEntity extends MetadataEntity {
    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    private BigDecimal fare;

    @NotEmpty
    @Valid
    @ElementCollection
    @CollectionTable(name = "flight_coupons", joinColumns = @JoinColumn(name = "transaction_id"))
    private List<Coupon> coupons = new ArrayList<>();

    public FlightMetadataEntity() {
    }

    @Override
    public TxnType type() { return TxnType.flight; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getFare() { return fare; }
    public void setFare(BigDecimal fare) { this.fare = fare; }
    public List<Coupon> getCoupons() { return coupons; }
    public void setCoupons(List<Coupon> coupons) { this.coupons = coupons; }
}
