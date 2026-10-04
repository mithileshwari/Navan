package com.navan.billing.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Embeddable
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Coupon {
    @NotBlank
    private String flightNumber;

    private String carrier;

    @JsonProperty("from")
    @Column(name = "from_airport")
    @NotBlank
    private String fromAirport;

    @JsonProperty("to")
    @Column(name = "to_airport")
    @NotBlank
    private String toAirport;

    @NotNull
    private Instant departure;

    @NotNull
    private Instant arrival;

    public Coupon() {
    }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getFromAirport() { return fromAirport; }
    public void setFromAirport(String fromAirport) { this.fromAirport = fromAirport; }
    public String getToAirport() { return toAirport; }
    public void setToAirport(String toAirport) { this.toAirport = toAirport; }
    public Instant getDeparture() { return departure; }
    public void setDeparture(Instant departure) { this.departure = departure; }
    public Instant getArrival() { return arrival; }
    public void setArrival(Instant arrival) { this.arrival = arrival; }
}
