package com.navan.billing.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.navan.billing.config.JsonNodeConverter;
import com.navan.billing.domain.TxnType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hotel_details")
@PrimaryKeyJoinColumn(name = "transaction_id")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class HotelMetadataEntity extends MetadataEntity {
    @NotBlank
    private String name;

    @NotNull
    @Valid
    @Embedded
    private Address address;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @NotNull
    @Min(1)
    private Integer nights;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    private BigDecimal roomRatePerNight;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "extras_json")
    private JsonNode extras;

    public HotelMetadataEntity() {
    }

    @AssertTrue(message = "check_out must be after check_in")
    public boolean isCheckOutAfterCheckIn() {
        return checkIn == null || checkOut == null || checkOut.isAfter(checkIn);
    }

    @Override
    public TxnType type() { return TxnType.hotel; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public Integer getNights() { return nights; }
    public void setNights(Integer nights) { this.nights = nights; }
    public BigDecimal getRoomRatePerNight() { return roomRatePerNight; }
    public void setRoomRatePerNight(BigDecimal roomRatePerNight) { this.roomRatePerNight = roomRatePerNight; }
    public JsonNode getExtras() { return extras; }
    public void setExtras(JsonNode extras) { this.extras = extras; }
}
