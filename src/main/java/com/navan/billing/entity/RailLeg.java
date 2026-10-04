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
public class RailLeg {
    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotNull
    private Instant departure;

    @NotNull
    private Instant arrival;

    private String trainNumber;

    @JsonProperty("class")
    @Column(name = "service_class")
    private String serviceClass;

    public RailLeg() {
    }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Instant getDeparture() { return departure; }
    public void setDeparture(Instant departure) { this.departure = departure; }
    public Instant getArrival() { return arrival; }
    public void setArrival(Instant arrival) { this.arrival = arrival; }
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public String getServiceClass() { return serviceClass; }
    public void setServiceClass(String serviceClass) { this.serviceClass = serviceClass; }
}
